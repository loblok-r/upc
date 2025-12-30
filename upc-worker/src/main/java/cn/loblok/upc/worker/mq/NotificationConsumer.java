package cn.loblok.upc.worker.mq;

import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.api.worker.dto.EmailMsgDTO;
import cn.loblok.upc.api.worker.dto.NotificationMsg;
import cn.loblok.upc.worker.config.RabbitConfig;
import cn.loblok.upc.worker.service.MailService;
import cn.loblok.upc.worker.service.NotifyService;
import cn.loblok.upc.worker.util.MessageRetryHelper;
import com.rabbitmq.client.Channel;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;



@Component
@Slf4j
@AllArgsConstructor
public class NotificationConsumer {

    private final MailService mailService;

    private final NotifyService notifyService;

    private final MessageRetryHelper retryHelper;


    /**
     * 1. 监听邮件队列 (验证码、欢迎邮件)
     */
    @RabbitListener(queues = MQConstants.QUEUE_EMAIL
            ,ackMode = "MANUAL")
    public void onEmailMessage(EmailMsgDTO msg, Channel channel, Message message) throws MessagingException {
        log.info("【邮件队列】开始处理任务: target={}, type={}", msg.getEmail(), msg.getType());
        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    try {
                        mailService.sendAsync(msg);
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                },
                MQConstants.DLX_EXCHANGE_NAME,
                MQConstants.ROUTE_EMAIL,
                2
        );
    }

    /**
     * 2. 监听站内信队列 (点赞、评论提醒)
     */
    @RabbitListener(queues = MQConstants.QUEUE_SITE_MSG
            , ackMode = "MANUAL")
    public void onSiteMessage(NotificationMsg msg, Message message, Channel channel) {
        log.info("【站内信队列】收到提醒消息: from={}, to={}, type={}",
                msg.getFromUserId(), msg.getToUserId(), msg.getType());

        retryHelper.processWithRetry(
                message,
                channel,
                () -> {
                    notifyService.createNotification(msg);
                },
                MQConstants.DLX_EXCHANGE_NAME,
                MQConstants.ROUTE_SITE_MSG,
                2
        );
    }
}
