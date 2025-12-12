package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.*;
import cn.loblok.upc.enums.CommonStatusEnum;
import cn.loblok.upc.exception.DailyLimitExceededException;
import cn.loblok.upc.exception.InsufficientComputingPowerException;
import cn.loblok.upc.service.AiService;
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

}