package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.CheckinRequestDTO;
import cn.loblok.upc.dto.CheckinResponseDTO;
import cn.loblok.upc.entity.CheckinRecord;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.CheckinRecordMapper;
import cn.loblok.upc.service.CheckinRecordService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Service
public class CheckinRecordServiceImpl extends ServiceImpl<CheckinRecordMapper, CheckinRecord> implements CheckinRecordService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public CheckinResponseDTO checkin(String tenantId, CheckinRequestDTO request) {
        CheckinResponseDTO response = new CheckinResponseDTO();
        
        // 获取用户信息
        User user = userService.getById(request.getUserId());
        if (user == null) {
            response.setSuccess(false);
            response.setMessage("用户不存在");
            return response;
        }
        
        // 获取当前日期
        LocalDate checkinDate = LocalDate.now();
        String dateStr = checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 构造 biz_key
        String bizKey = "checkin_" + tenantId + "_" + request.getUserId() + "_" + dateStr;
        
        // 检查是否已经签到
        QueryWrapper<CheckinRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("biz_key", bizKey);
        if (this.count(queryWrapper) > 0) {
            response.setSuccess(false);
            response.setMessage("今日已签到");
            return response;
        }
        
        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setUserId(request.getUserId());
        checkinRecord.setTenantId(tenantId);
        checkinRecord.setCheckinDate(checkinDate);
        checkinRecord.setBizKey(bizKey);
        // 这里可以添加IP地址和User-Agent，但在实际应用中应该从请求中获取
        checkinRecord.setIpAddress("192.168.1.100"); // 示例IP地址
        checkinRecord.setUserAgent("Mozilla/5.0 ..."); // 示例User-Agent
        
        // 插入签到记录
        boolean saveResult = this.save(checkinRecord);
        if (!saveResult) {
            response.setSuccess(false);
            response.setMessage("签到失败");
            return response;
        }
        
        // 设置响应
        response.setSuccess(true);
        response.setCheckinRecordId(checkinRecord.getId());
        response.setMessage("签到成功");
        
        return response;
    }
}