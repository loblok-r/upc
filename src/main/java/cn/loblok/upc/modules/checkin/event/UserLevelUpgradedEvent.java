package cn.loblok.upc.modules.checkin.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户等级升级事件
 */
@Getter
public class UserLevelUpgradedEvent extends ApplicationEvent {
    
    private final Long userId;
    private final int oldLevel;
    private final int newLevel;
    private final String oldLevelName;
    private final String newLevelName;
    
    public UserLevelUpgradedEvent(Object source, Long userId, int oldLevel, int newLevel, String oldLevelName, String newLevelName) {
        super(source);
        this.userId = userId;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.oldLevelName = oldLevelName;
        this.newLevelName = newLevelName;
    }
}