package cn.loblok.upc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */

/**
 * 签到记录
 */
@Getter
@Setter
@TableName("checkin_record")
public class CheckinRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("checkin_date")
    private LocalDate checkinDate;

    @TableField("biz_key")
    private String bizKey;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
