package cn.loblok.upc.controller;

import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.service.CheckinRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@RestController
@RequestMapping("/api")
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
    public CheckinResponseDTO checkin(@RequestHeader("X-Tenant-ID") String tenantId,
                                      @RequestBody CheckinRequestDTO request) {
        return checkinRecordService.checkin(tenantId, request);
    }
}