package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.AiGenerateRequest;
import cn.loblok.upc.dto.AiGenerateResponse;
import cn.loblok.upc.entity.DailyUsage;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.enums.AppMode;
import cn.loblok.upc.exception.DailyLimitExceededException;
import cn.loblok.upc.exception.InsufficientComputingPowerException;
import cn.loblok.upc.mapper.DailyUsageMapper;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.AiService;
import cn.loblok.upc.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * AI功能实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AiGenerateImpl implements AiService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final DailyUsageMapper dailyUsageMapper;
    // private final UsageLogMapper usageLogMapper; // 可选


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
        // 调用 AI 模型
        AiResult aiResult = callAiModel(mode, prompt, refImage);

        log.info("生成完成，结果: {}", aiResult);
        // 扣减算力（事务内）
        user.setComputingPower(user.getComputingPower() - cost);
        userMapper.updateById(user);

            log.info("已扣除算力: {}", cost);
        // todo 记录日志
        // logUsage(userId, mode, cost, prompt);


        // 构造响应
        String type = mode == AppMode.TEXT_CHAT ? "text" : "image";


        return new AiGenerateResponse(type, aiResult.getContent(), aiResult.getImageUrl());
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
            try{
                dailyUsageMapper.insert(usage);
            }catch (Exception e){
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
            case TEXT_CHAT -> 20;
            case AI_DRAWING -> 5;
            default -> 10;
        };
    }

    private void incrementDailyUsage(Long userId, AppMode mode) {
        dailyUsageMapper.incrementUsage(String.valueOf(userId), LocalDate.now(), mode.name());
    }

    // 模拟 AI 调用
    private AiResult callAiModel(AppMode mode, String prompt, String refImage) {
        if (mode == AppMode.TEXT_CHAT) {
            return new AiResult("这是由 UPC AI 生成的回答：" + prompt, null);
        } else {
            // 返回一个示例图片 URL（实际应调用 Stable Diffusion / DALL·E 等）
            return new AiResult(null, "https://images.unsplash.com/photo-1506905925346-21bda4d32df4");
        }
    }

    @Data
    @AllArgsConstructor
    private static class AiResult {
        private String content;
        private String imageUrl;
    }
}