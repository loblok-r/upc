package cn.loblok.upc.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.upc.ai.client.QwenClient;
import cn.loblok.upc.ai.client.SilionClient;
import cn.loblok.upc.ai.dto.AiGenerateRequest;
import cn.loblok.upc.ai.dto.AiGenerateResponse;
import cn.loblok.upc.ai.dto.AiResult;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.AiSettleDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.ai.service.AiService;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.exception.DailyLimitExceededException;
import cn.loblok.upc.common.exception.InsufficientComputingPowerException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * AI功能实现类
 */
@Service
@RequiredArgsConstructor
//@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AiGenerateImpl implements AiService {


    private final QwenClient qwenClient;
    private final SilionClient silionClient;

    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;


    @Override
    public AiGenerateResponse generate(Long userId, AiGenerateRequest req) {

        log.info("正在为用户{}生成内容... , mode: {}, prompt:{}, refImage: {}", userId, req.getMode(), req.getPrompt(), req.getReferenceImage());


        AppMode mode = AppMode.valueOf(req.getMode());
        String prompt = req.getPrompt();
        String refImage = req.getReferenceImage();
        // 计算成本
        int cost = calculateCost(mode, prompt, refImage);

        log.info("计算成本: {}", cost);

        // 判断权限
        Result<Void> checkResult = userFeignClient.checkAiAccess(userId, mode, cost);

        if (checkResult.getCode() != 200) {
            // AI 模块直接根据 User 模块返回的 code 进行响应
            // 你也可以根据不同的 code 抛出不同的本地 Exception
            if(checkResult.getCode() == CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getCode()){
                throw new InsufficientComputingPowerException("算力不足");
            }else if (checkResult.getCode() == CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getCode()){
                throw new DailyLimitExceededException("达到日限额");
            }
        }

        log.info("验权通过，开始调用 AI 模型...");
//         调用 AI 模型
        AiResult aiResult = null;
        try {

            aiResult = callAiModel(mode, prompt,req.getSize(), refImage,userId);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }


        log.info("生成完成，结果: {}", aiResult);

        //判断是否开启新会话
        String sessionId = req.getSessionId() == null ? IdUtil.randomUUID() : req.getSessionId();


        /** todo AI 服务在发送 MQ 之前，可以先在本地 Redis 存一个“待结算任务”。
         如果 MQ 发送成功并得到 Ack，就删掉。如果 1 分钟后还没删，说明没发出去，
         由 AI 服务的定时任务补发。**/

        AiSettleDTO aiSettleDTO = new AiSettleDTO();
        aiSettleDTO.builder()
                .userId(userId)
                .cost(cost)
                .mode(mode)
                .sessionId(sessionId)
                .prompt(prompt)
                .refImage(refImage)
                .content(aiResult.getContent())
                .cosPath(aiResult.getCosPath())
                .build();

        rabbitTemplate.convertAndSend("upc.direct.exchange", "mq.route.ai_settle", aiSettleDTO);


        // 构造响应
        String type = mode == AppMode.TEXT_CHAT ? "text" : "image";



        return new AiGenerateResponse(type, aiResult.getContent(), aiResult.getImageUrl(),aiResult.getCosPath(),sessionId,req.getWidth(),req.getHeight());
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


    private AiResult callAiModel(AppMode mode, String prompt,String size, String refImage, Long userID) throws NoApiKeyException, InputRequiredException {
        if (mode == AppMode.TEXT_CHAT) {
            String text =  qwenClient.generateText(prompt);
            return new AiResult("这是由 UPC AI 生成的回答：" + text, null,null);
        } else {
            AiResult result = silionClient.generateImage(userID, prompt, size,refImage);
            return result;
        }
    }


}