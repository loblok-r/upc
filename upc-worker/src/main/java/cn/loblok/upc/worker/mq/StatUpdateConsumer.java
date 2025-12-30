package cn.loblok.upc.worker.mq;


import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.worker.service.StatService;
import cn.loblok.upc.worker.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class StatUpdateConsumer {


    private final StatService statService;

    private final RedisTemplate<String, String> redisTemplate;


    private final MessageRetryHelper retryHelper;


    @RabbitListener(queues = MQConstants.QUEUE_STATS_UPDATE
            ,ackMode = "MANUAL")
    public void onStatUpdateMessage(StatUpdateMsgDTO msg, Message message, Channel channel) {
        log.info("【统计更新】收到任务: userId={}, type={}, delta={}",
                msg.getUserId(), msg.getType(), msg.getDelta());

        if(msg.getUserId() == null){
            log.warn("用户ID为空");
            return;
        }
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    String latestKey = KeyUtils.buildPostTabLatestPostKey();
                    String recommendKey = KeyUtils.buildPostTabRecommendPostKey();
                    String detailKey = KeyUtils.buildPostDetailKey(msg.getPostId());
                    switch (msg.getType()) {
                        case "LIKE":     // 获赞
                            statService.updateUserLikes(msg.getUserId(), msg.getDelta());
                            if (msg.getPostId() != null) {
                                // 使用 incrementScore，点赞 delta 为 1，取消点赞 delta 为 -1
                                // Redis 会重新对这 2000 条帖子进行排序
                                redisTemplate.opsForZSet().incrementScore(recommendKey, msg.getPostId().toString(), msg.getDelta());
                                //删除详情缓存，让其“懒加载”更新点赞数
                                redisTemplate.delete(detailKey);
                            }
                            break;
                        case "POST":     // 作品数
                            statService.updateUserWorks(msg.getUserId(), msg.getDelta());
                            if (msg.getPostId() != null && msg.getDelta() > 0){
                                redisTemplate.opsForZSet().add(latestKey, msg.getPostId().toString(), System.currentTimeMillis());
                                redisTemplate.opsForZSet().add(recommendKey, msg.getPostId().toString(), 0.0);
                                // 保持 ZSet 长度，比如只留 2000 条，防止内存溢出
                                redisTemplate.opsForZSet().removeRange(latestKey, 0, -2001);
                            }else if(msg.getPostId() != null && msg.getDelta() < 0){
                                // 删除帖子
                                redisTemplate.opsForZSet().remove(latestKey, msg.getPostId().toString());
                                redisTemplate.opsForZSet().remove(recommendKey, msg.getPostId().toString());
                                redisTemplate.delete(detailKey);
                            }
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
                },
                MQConstants.RETRY_EXCHANGE_NAME,
                MQConstants.QUEUE_STATS_UPDATE + ".retry.5s",
                2 // 最多重试 2 次
        );
    }
}