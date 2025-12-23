package cn.loblok.upc.auth.controller.checkin;

import cn.loblok.upc.common.annotation.CurrentUser;
import cn.loblok.upc.auth.dto.chickin.CheckinHistoryResponse;
import cn.loblok.upc.auth.dto.chickin.RetroRequest;
import cn.loblok.upc.common.enums.CommonStatusEnum;
import cn.loblok.upc.auth.service.chickin.CheckinRecordService;
import cn.loblok.upc.auth.dto.chickin.CheckinResponseDTO;
import cn.loblok.upc.common.base.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * <p>
 *      用户签到接口
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/api/checkin")
@AllArgsConstructor
@Slf4j
public class CheckinRecordController {
    

    private final CheckinRecordService checkinRecordService;

    private static final String TEMP_TENANT_ID = "default";
    
    /**
     * 用户签到接口
     * @param tenantId 租户ID
     * @return 签到响应
     */
    @PostMapping("/checkin")
    public Result<CheckinResponseDTO> checkin(@RequestHeader("X-Tenant-ID") String tenantId,
                                              @CurrentUser Long userId) {
        //暂不启用
        tenantId = TEMP_TENANT_ID;
        return checkinRecordService.checkin(tenantId, userId);
    }


    @GetMapping("/history")
    public Result<CheckinHistoryResponse> getCheckinHistory(@CurrentUser Long userId) {
        CheckinHistoryResponse response = checkinRecordService.getRecentCheckinHistory(userId,30);
        return Result.success(response);
    }

    /**
     * 查询今日是否已签到（用于前端展示按钮状态）
     */
    @GetMapping("/status")
    public Result<Boolean> getCheckinStatus(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam("userId") Long userId) {

        //暂不启用
        tenantId = TEMP_TENANT_ID;

        boolean checked = checkinRecordService.hasCheckedInToday(tenantId, userId);
        return Result.success(checked);
    }


    @PostMapping("/retro")
    public Result<CheckinResponseDTO> reTroChickIn(@CurrentUser Long userId,@RequestBody RetroRequest request) {
        log.info("RetroRequest: {}", request);
        //暂不启用
        String tenantId = TEMP_TENANT_ID;
        LocalDate retroDate = request.getRetroDate();
        if(retroDate == null){
            return Result.error(CommonStatusEnum.CANT_RETRO_DATE_NULL.getCode(),CommonStatusEnum.CANT_RETRO_DATE_NULL.getMessage());
        }
        return checkinRecordService.reTroChickIn(tenantId,userId,retroDate);
    }

}