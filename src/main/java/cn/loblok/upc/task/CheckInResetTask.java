package cn.loblok.upc.task;

import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class CheckInResetTask {
    @Autowired
    private UserMapper userMapper;

    /**
     * 每天00:00重置签到状态
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void resetDailyCheckin() {
        log.info("开始执行每日签到状态重置任务: {}", LocalDateTime.now());

        try {
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("checked_in", 0)
                    .set("update_time", LocalDateTime.now())
                    .eq("checked_in", 1);

            int affectedRows = userMapper.update(null, updateWrapper);

            log.info("签到状态重置完成，重置用户数: {}", affectedRows);

//            // 记录操作日志到数据库（可选）
//            logResetOperation(affectedRows);

        } catch (Exception e) {
            log.error("重置签到状态失败", e);
            throw e; // 触发事务回滚
        }
    }

    /**
     * 凌晨01:00额外检查重置（兜底方案）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void backupResetCheckin() {
        log.info("执行签到状态兜底重置检查: {}", LocalDateTime.now());

        // 检查当前时间是否已经过了凌晨，如果是，确保所有用户checked_in=0
        LocalDateTime now = LocalDateTime.now();
        if (now.toLocalTime().isAfter(LocalTime.of(0, 30))) {
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("checked_in", 0)
                    .set("update_time", now)
                    .ge("checked_in", 1); // 大于等于1的都重置
            int affectedRows = userMapper.update(null, updateWrapper);

            if (affectedRows > 0) {
                log.warn("兜底重置触发，重置了 {} 个异常的签到状态", affectedRows);
            }
        }
    }

    /**
     * 每30分钟检查是否需要重置（针对跨时区用户）
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkContinuousSignin() {

        //todo  可以在这里检查连续签到是否中断
        // 比如：如果用户昨天没签到，今天应该重置连续签到天数

        log.debug("检查连续签到状态: {}", LocalDateTime.now());
    }

}