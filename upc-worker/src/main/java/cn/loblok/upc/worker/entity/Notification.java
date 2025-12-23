package cn.loblok.upc.worker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    /**
     * 类型见消息契约，如 LIKE, COMMENT, FOLLOW
     */
    private String type;

    /**
     * 关联的业务ID
     */
    private Long targetId;

    /**
     * 消息预览内容
     */
    private String content;

    /**
     * 0-未读，1-已读
     */
    private Integer isRead;

    private LocalDateTime createTime;
}