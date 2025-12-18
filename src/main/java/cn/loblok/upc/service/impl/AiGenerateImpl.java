package cn.loblok.upc.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.upc.client.QwenClient;
import cn.loblok.upc.client.SilionClient;
import cn.loblok.upc.dto.AiGenerateRequest;
import cn.loblok.upc.dto.AiGenerateResponse;
import cn.loblok.upc.dto.AiResult;
import cn.loblok.upc.dto.InvoiceSummaryRequest;
import cn.loblok.upc.entity.AiGenerationLogs;
import cn.loblok.upc.entity.DailyUsage;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.enums.AppMode;
import cn.loblok.upc.enums.DailyLimitEnum;
import cn.loblok.upc.enums.MessageContentType;
import cn.loblok.upc.exception.DailyLimitExceededException;
import cn.loblok.upc.exception.InsufficientComputingPowerException;
import cn.loblok.upc.mapper.DailyUsageMapper;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.AiGenerationLogsService;
import cn.loblok.upc.service.AiService;
import cn.loblok.upc.service.UserService;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI功能实现类
 */
@Service
@RequiredArgsConstructor
//@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AiGenerateImpl implements AiService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final DailyUsageMapper dailyUsageMapper;
    // private final UsageLogMapper usageLogMapper; // 可选
    private final AiGenerationLogsService aiGenerationLogsMapper;
    private final QwenClient qwenClient;
    private final SilionClient silionClient;


    @Override
    public AiGenerateResponse generate(Long userId, AiGenerateRequest req) {

        log.info("正在为用户{}生成内容... , mode: {}, prompt:{}, refImage: {}", userId, req.getMode(), req.getPrompt(), req.getReferenceImage());

        AppMode mode = parseMode(req.getMode());
        String prompt = req.getPrompt();
        String refImage = req.getReferenceImage();

        // 获取用户（实时 DB 查询）
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("User not found");

        // 计算成本
        int cost = calculateCost(mode, prompt, refImage);

        log.info("计算成本: {}", cost);
        //校验算力
        if (user.getComputingPower() < cost) {
            throw new InsufficientComputingPowerException("INSUFFICIENT_COMPUTING_POWER");
        }


        // 校验日限额（仅非会员）
        checkAndIncrementDailyUsage(userId, mode);

        log.info("开始调用 AI 模型...");
//         调用 AI 模型
        AiResult aiResult = null;
        try {
            aiResult = callAiModel(mode, prompt, refImage,userId);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }

        log.info("生成完成，结果: {}", aiResult);
        // 扣减算力（事务内）
        user.setComputingPower(user.getComputingPower() - cost);
        userMapper.updateById(user);

        log.info("已扣除算力: {}", cost);

        //判断是否开启新会话
        String sessionId = req.getSessionId() == null ? IdUtil.randomUUID() : req.getSessionId();

        //记录日志
        AiGenerationLogs aiGenerationLogs = new AiGenerationLogs();
        aiGenerationLogs.setId(IdUtil.randomUUID());
        aiGenerationLogs.setUserId(userId);
        aiGenerationLogs.setSessionId(sessionId);
        aiGenerationLogs.setPrompt(prompt);
        aiGenerationLogs.setContentType(mode == AppMode.TEXT_CHAT ? MessageContentType.TEXT : MessageContentType.IMAGE);
        aiGenerationLogs.setCost(cost);
        aiGenerationLogs.setParams(req.getReferenceImage());
        aiGenerationLogs.setStatus("SUCCESS");
        if(mode == AppMode.TEXT_CHAT){
            aiGenerationLogs.setResultUrl(aiResult.getContent());
        }else {
            aiGenerationLogs.setResultUrl(aiResult.getCosPath());
        }
        aiGenerationLogs.setCreatedAt(LocalDateTime.now());

        try{
            aiGenerationLogsMapper.save(aiGenerationLogs);
        }catch (Exception e){
            log.error("保存日志失败: {}", e.getMessage());
        }
        // 构造响应
        String type = mode == AppMode.TEXT_CHAT ? "text" : "image";


        return new AiGenerateResponse(type, aiResult.getContent(), aiResult.getImageUrl(),aiResult.getCosPath(),sessionId);
    }

    @Override
    public String generateInvoiceSummary(Long userId, InvoiceSummaryRequest request) {


        return "Invoice summary generated successfully";
    }


    private AppMode parseMode(String modeStr) {
        try {
            return AppMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid mode: " + modeStr);
        }
    }

    private int calculateCost(AppMode mode, String prompt, String refImage) {
        return switch (mode) {
            case TEXT_CHAT -> Math.max(1, (int) Math.ceil(prompt.length() / 100.0));
            case AI_DRAWING -> 10 + (refImage != null ? 5 : 0);
            case SMART_PRESENTATION -> 15;
            case PODCAST -> 20;
            default -> 5;
        };
    }

    private void checkAndIncrementDailyUsage(Long userId, AppMode mode) {

        log.info("检查用户{}的日限额...{}", userId);
        if (Boolean.TRUE.equals(userService.isMember(userId))) {
            // 会员不限制，但仍需记录次数（用于统计）
            log.info("用户{}是会员，不检查日限额", userId);
            incrementDailyUsage(userId, mode);
            return;
        }
        log.info("用户{}非会员，开始检查日限额...", userId);

        LocalDate today = LocalDate.now();
        DailyUsage usage = dailyUsageMapper.selectByUserIdAndDate(String.valueOf(userId), today);
        if (usage == null) {
            usage = new DailyUsage();
            usage.setUserId(String.valueOf(userId));
            usage.setDate(today);
            usage.setTextChatCount(0);
            usage.setAiDrawingCount(0);
            try {
                dailyUsageMapper.insert(usage);
            } catch (Exception e) {
                log.error("插入日用量失败: {}", e.getMessage());
            }

        }

        int current = getCurrentCount(usage, mode);
        int limit = getDailyLimit(mode);

        log.info("用户{}的日限额情况: {}", userId, usage);
        if (current >= limit) {
            throw new DailyLimitExceededException("DAILY_LIMIT_EXCEEDED");
        }

        incrementDailyUsage(userId, mode);
    }

    private int getCurrentCount(DailyUsage usage, AppMode mode) {
        return switch (mode) {
            case TEXT_CHAT -> usage.getTextChatCount();
            case AI_DRAWING -> usage.getAiDrawingCount();
            default -> 0;
        };
    }

    private int getDailyLimit(AppMode mode) {
        return switch (mode) {
            case TEXT_CHAT -> DailyLimitEnum.TEXT_CHAT.getLimit();
            case AI_DRAWING -> DailyLimitEnum.AI_DRAWING.getLimit();
            default -> DailyLimitEnum.Default.getLimit();
        };
    }

    private void incrementDailyUsage(Long userId, AppMode mode) {
        dailyUsageMapper.incrementUsage(String.valueOf(userId), LocalDate.now(), mode.name());
    }


    private AiResult callAiModel(AppMode mode, String prompt, String refImage, Long userID) throws NoApiKeyException, InputRequiredException {
        if (mode == AppMode.TEXT_CHAT) {
            String text =  qwenClient.generateText(prompt);
            return new AiResult("这是由 UPC AI 生成的回答：" + text, null,null);
        } else {
            AiResult result = silionClient.generateImage(userID, prompt, refImage);
            return result;
        }
    }


}