package cn.loblok.upc.controller;

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
     * @param request 签到请求
     * @return 签到响应
     */
    @PostMapping("/checkin")
    public Result<CheckinResponseDTO> checkin(@RequestHeader("X-Tenant-ID") String tenantId,
                          @RequestBody CheckinRequestDTO request) {
        return checkinRecordService.checkin(tenantId, request);
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