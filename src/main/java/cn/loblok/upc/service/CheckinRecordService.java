package cn.loblok.upc.service;

import cn.loblok.upc.dto.CheckinHistoryResponse;
import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.CheckinRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

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
     * @param
     * @return 签到响应
     */
    Result<CheckinResponseDTO> checkin(String tenantId, Long userId);

    Result<CheckinResponseDTO> executeCheckin(String tenantId, Long userId,LocalDate retroDate);

    boolean hasCheckedInToday(String tenantId, Long userId);

    CheckinHistoryResponse getRecentCheckinHistory(long userid, int days);

    Result<CheckinResponseDTO> reTroChickIn(Long userId, LocalDate retroDate);
}