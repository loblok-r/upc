package cn.loblok.upc.modules.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String username;
//    private final String tenantId;
    
    public UserRegisteredEvent(Object source, Long userId, String username) {
        super(source);
        this.userId = userId;
        this.username = username;
//        this.tenantId = tenantId;
    }
}