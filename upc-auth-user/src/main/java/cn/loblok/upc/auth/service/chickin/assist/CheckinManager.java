package cn.loblok.upc.auth.service.chickin.assist;

import cn.loblok.upc.auth.common.util.CaculateUtils;
import cn.loblok.upc.auth.dto.chickin.RewardResult;
import cn.loblok.upc.auth.entity.User;
import cn.loblok.upc.auth.entity.UserPoints;
import cn.loblok.upc.auth.entity.chickin.CheckinRecord;
import cn.loblok.upc.auth.mapper.CheckinRecordMapper;
import cn.loblok.upc.auth.service.UserPointsService;
import cn.loblok.upc.auth.service.UserService;
import cn.loblok.upc.common.utils.KeyUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 核心事务与持久化管理器
 */
@Component
@AllArgsConstructor
public class CheckinManager {
    private final CheckinRecordMapper checkinRecordMapper;
    private final UserPointsService userPointsService;
    private final UserService userService;
    private final CaculateUtils caculateUtils;

    @Transactional(rollbackFor = Exception.class)
    public long persistCheckinData(String tenantId, User user, LocalDate date, RewardResult rewards, int streakDays) {
        String bizKey = KeyUtils.buildCheckinBizKey(tenantId, user.getId(), date.toString());

        // 1. 记录落库
        CheckinRecord record = new CheckinRecord();
        record.setUserId(user.getId());
        record.setTenantId(tenantId);
        record.setCheckinDate(date);
        record.setBizKey(bizKey);
        checkinRecordMapper.insert(record);

        // 2. 累加积分表 (使用 SQL 层面加法防止并发覆盖)
        userPointsService.update(null, new UpdateWrapper<UserPoints>()
                .eq("user_id", user.getId())
                .setSql("balance = balance + " + rewards.getTotalPoints())
                .setSql("total_earned = total_earned + " + rewards.getTotalPoints())
                .set("updated_at", LocalDateTime.now())
        );

        // 3. 更新用户主表 (经验、等级、连续天数、签到标记)
        String newLevel = caculateUtils.calculateLevel(rewards.getNewTotalExp());

        userService.update(null, new UpdateWrapper<User>()
                .eq("id", user.getId())
                .set("ischickined", 1)
                .setSql("points = points + " + rewards.getTotalPoints())
                .set("exp", rewards.getNewTotalExp())
                .set("user_level", newLevel)
                .set("streakdays", streakDays)
        );

        return record.getId();
    }


}