package cn.loblok.upc.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.util.rabbit.constants.MQConstants;
import cn.loblok.upc.ai.client.QwenClient;
import cn.loblok.upc.ai.client.SilionClient;
import cn.loblok.upc.ai.dto.AiGenerateRequest;
import cn.loblok.upc.ai.dto.AiGenerateResponse;
import cn.loblok.upc.ai.dto.AiResult;
import cn.loblok.upc.ai.service.assist.AiCostCalculator;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.AiSettleDTO;
import cn.loblok.upc.common.annotation.RequireAiQuota;
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
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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
    private final RabbitTemplate rabbitTemplate;
    private final AiCostCalculator aiCostCalculator;


    @Override
    @RequireAiQuota(mode = "#req.mode")
    public AiGenerateResponse generate(Long userId, AiGenerateRequest req) {

        log.info("正在为用户{}生成内容... , mode: {}, prompt:{}, refImage: {}", userId, req.getMode(), req.getPrompt(), req.getReferenceImage());


        AppMode mode = AppMode.fromMode(req.getMode());
        // 计算成本
        int cost = aiCostCalculator.calculate(mode, req.getPrompt(), req.getReferenceImage());

        log.info("计算成本: {}", cost);



        log.info("验权通过，开始调用 AI 模型...");
//         调用 AI 模型
        AiResult aiResult = null;
        try {

            aiResult = callAiModel(mode, req.getPrompt(),req.getSize(), req.getReferenceImage(),userId);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }


        log.info("生成完成，结果: {}", aiResult);

        String sessionId = sendSettleMessage(userId, req, cost, mode,aiResult);


        // 构造响应
        String type = mode == AppMode.TEXT_CHAT ? "text" : "image";



        return new AiGenerateResponse(type, aiResult.getContent(), aiResult.getImageUrl(),aiResult.getCosPath(),sessionId,req.getWidth(),req.getHeight());
    }

    private String sendSettleMessage(Long userId, AiGenerateRequest req, int cost, AppMode mode, AiResult aiResult) {
        //判断是否开启新会话
        String sessionId = req.getSessionId() == null ? IdUtil.randomUUID() : req.getSessionId();


        /** todo AI 服务在发送 MQ 之前，可以先在本地 Redis 存一个“待结算任务”。
         如果 MQ 发送成功并得到 Ack，就删掉。如果 1 分钟后还没删，说明没发出去，
         由 AI 服务的定时任务补发。**/

        AiSettleDTO aiSettleDTO = AiSettleDTO.builder()
                .userId(userId)
                .cost(cost)
                .mode(mode)
                .sessionId(sessionId)
                .prompt(req.getPrompt())
                .refImage(req.getReferenceImage())
                .content(aiResult.getContent())
                .cosPath(aiResult.getCosPath())
                .build();


        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);

        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_AI_SETTLE,
                aiSettleDTO,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );
        return sessionId;
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