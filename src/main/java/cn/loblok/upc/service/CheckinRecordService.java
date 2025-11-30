package cn.loblok.upc.service;

import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.entity.CheckinRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
public interface CheckinRecordService extends IService<CheckinRecord> {
    
    /**
     * 用户签到
     * @param tenantId 租户ID
     * @param request 签到请求
     * @return 签到响应
     */
    CheckinResponseDTO checkin(String tenantId, CheckinRequestDTO request);
}