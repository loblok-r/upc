package cn.loblok.upc.modules.ai.controller;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.common.exception.DailyLimitExceededException;
import cn.loblok.upc.common.exception.InsufficientComputingPowerException;
import cn.loblok.upc.modules.ai.dto.AiGenerateRequest;
import cn.loblok.upc.modules.ai.dto.AiGenerateResponse;
import cn.loblok.upc.modules.ai.service.AiService;
import cn.loblok.upc.modules.payment.dto.InvoiceSummaryRequest;
import cn.loblok.upc.modules.payment.dto.SummaryResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 图片生成控制器
 */
@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiGenerateService;

    @PostMapping("/generate")
    public Result<AiGenerateResponse> generate(
            @RequestBody @Valid AiGenerateRequest request,
            @CurrentUser Long userId) {

        log.info("用户ID: {}, 请求参数: {}", userId, request);


        try {
            AiGenerateResponse response = aiGenerateService.generate(userId, request);
            return Result.success(response);
        } catch (InsufficientComputingPowerException e) {
            return Result.error(CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getCode(),
                    CommonStatusEnum.INSUFFICIENT_COMPUTING_POWER.getMessage());
        } catch (DailyLimitExceededException e) {
            return Result.error(CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getCode(),
                    CommonStatusEnum.DAILY_LIMIT_EXCEEDED.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(CommonStatusEnum.INVALID_ARGUMENT.getCode(),
                    CommonStatusEnum.INVALID_ARGUMENT.getMessage());
        } catch (Exception e) {
            // 记录日志
            return Result.error(CommonStatusEnum.INTERNAL_SERVER_ERROR.getCode(),
                    CommonStatusEnum.INTERNAL_SERVER_ERROR.getMessage());
        }

    }


    @PostMapping("/generateInvoiceSummary")
    public Result<SummaryResponse> generateInvoiceSummary(
            @RequestBody  @Valid InvoiceSummaryRequest request,
            @CurrentUser Long userId) {

        log.info("用户ID: {}, 创建发票摘要请求参数: {}", userId, request);


        // 1. 获取数据
        String userName = request.getFormData().getLastName() + request.getFormData().getFirstName();
        String planName = request.getOrderDetails().getPlanName();


        String summary = aiGenerateService.generateInvoiceSummary(userId, request);
        // 模拟返回
//        String mockSummary = String.format("尊贵的 %s，您正在订阅 %s，价格为 %s %s。",
//                userName,
//                planName,
//                request.getOrderDetails().getPrice(),
//                request.getOrderDetails().getCurrency());
        SummaryResponse summaryResponse = new SummaryResponse();

        summaryResponse.setSummary(summary);
        return Result.success(summaryResponse);

    }



}