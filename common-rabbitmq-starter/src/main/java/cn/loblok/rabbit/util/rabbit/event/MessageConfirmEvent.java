package cn.loblok.rabbit.util.rabbit.event;

import org.springframework.context.ApplicationEvent;

public class MessageConfirmEvent extends ApplicationEvent {
    private final String bizId;
    private final boolean ack;
    private final String cause;

    public MessageConfirmEvent(Object source, String bizId, boolean ack, String cause) {
        super(source);
        this.bizId = bizId;
        this.ack = ack;
        this.cause = cause;
    }

    // getters
    public String getBizId() { return bizId; }
    public boolean isAck() { return ack; }
    public String getCause() { return cause; }
}