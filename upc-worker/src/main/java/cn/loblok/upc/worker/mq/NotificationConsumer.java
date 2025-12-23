package cn.loblok.upc.worker.mq;

import cn.loblok.upc.api.worker.dto.EmailMsgDTO;
import cn.loblok.upc.api.worker.dto.NotificationMsg;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.MailService;
import cn.loblok.upc.worker.service.NotifyService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    @Autowired
    private MailService mailService;

    @Autowired
    private NotifyService notifyService;

    /**
     * 1. 监听邮件队列 (验证码、欢迎邮件)
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void onEmailMessage(EmailMsgDTO msg) throws MessagingException {
        log.info("【邮件队列】开始处理任务: target={}, type={}", msg.getEmail(), msg.getType());
        try {
            mailService.sendAsync(msg);
        } catch (Exception e) {
            log.error("【邮件队列】发送失败，错误信息: {}", e.getMessage());
            // 抛出异常触发 MQ 的重试机制
            throw e;
        }
    }

    /**
     * 2. 监听站内信队列 (点赞、评论提醒)
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_SITE_MSG)
    public void onSiteMessage(NotificationMsg msg) {
        log.info("【站内信队列】收到提醒消息: from={}, to={}, type={}",
                msg.getFromUserId(), msg.getToUserId(), msg.getType());
        try {
            notifyService.createNotification(msg);
        } catch (Exception e) {
            log.error("【站内信队列】入库失败: {}", e.getMessage());
            // 如果入库失败，通常是数据库波动，重试几次即可
            throw e;
        }
    }


}