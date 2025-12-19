package cn.loblok.upc.task;


import cn.loblok.upc.modules.auth.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 定时发放奖励任务
 */
@Service
@Slf4j
@AllArgsConstructor
public class BonusTask {



    private final  UserService userService;
    /**
     * 每月定时发放算力值给会员用户
     * 每月1号凌晨执行，给所有会员用户发放1000点算力值
     */
// @Scheduled(cron = "0 0 0 1 * ?")
    public void distributeMonthlyBonus() {
        log.info("开始执行每月会员算力值发放任务");


        userService.list(new QueryWrapper<>()).forEach(user -> {

            if(userService.isMember(user.getId())) {
                userService.addComputePower(user.getId(), 1000);

                log.info("为用户ID={}的会员发放1000点算力值", user.getId());
                //TODO 记录发放流水
                //TODO 通知 会员

            }
        });

        log.info("每月会员算力值发放任务执行完成");
    }
}