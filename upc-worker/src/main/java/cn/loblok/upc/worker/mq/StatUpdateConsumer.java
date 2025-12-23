package cn.loblok.upc.worker.mq;


import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.StatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatUpdateConsumer {

    @Autowired
    private StatService statService;

    @RabbitListener(queues = RabbitConfig.QUEUE_STATS_UPDATE)
    public void onStatUpdateMessage(StatUpdateMsgDTO msg) {
        log.info("【统计更新】收到任务: userId={}, type={}, delta={}",
                msg.getUserId(), msg.getType(), msg.getDelta());

        try {
            switch (msg.getType()) {
                case "LIKE":     // 获赞
                    statService.updateUserLikes(msg.getUserId(), msg.getDelta());
                    break;
                case "POST":     // 作品数
                    statService.updateUserWorks(msg.getUserId(), msg.getDelta());
                    break;
                case "FOLLOW":   // 粉丝数
                    statService.updateUserFollowings(msg.getUserId(), msg.getDelta());
                    if (msg.getTargetUserId() != null) {
                        statService.updateUserFollowers(msg.getTargetUserId(), msg.getDelta());
                    }
                    break;
                default:
                    log.warn("未知统计类型: {}", msg.getType());
            }
        } catch (Exception e) {
            log.error("【统计更新】失败: {}", e.getMessage());
            // 抛出异常触发重试
            throw e;
        }
    }
}