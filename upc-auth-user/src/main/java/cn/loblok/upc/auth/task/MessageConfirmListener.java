package cn.loblok.upc.auth.task;

import cn.loblok.rabbit.event.MessageConfirmEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConfirmListener {

    @EventListener
    public void handleConfirm(MessageConfirmEvent event) {
        if (!event.isAck()) {
            // TODO: 记录失败日志、触发重试、告警等
            System.out.println("消息发送失败: " + event.getBizId() + ", 原因: " + event.getCause());
        }
    }
}