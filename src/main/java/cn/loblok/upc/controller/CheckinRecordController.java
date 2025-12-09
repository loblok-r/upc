package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.CheckinHistoryResponse;
import cn.loblok.upc.service.CheckinRecordService;
import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
public class CheckinRecordController {
    
    @Autowired
    private CheckinRecordService checkinRecordService;
    
    /**
     * 用户签到接口
     * @param tenantId 租户ID
     * @return 签到响应
     */
    @PostMapping("/checkin")
    public Result<CheckinResponseDTO> checkin(@RequestHeader("X-Tenant-ID") String tenantId,
                                              @CurrentUser Long userId) {
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

        boolean checked = checkinRecordService.hasCheckedInToday(tenantId, userId);
        return Result.success(checked);
    }
}