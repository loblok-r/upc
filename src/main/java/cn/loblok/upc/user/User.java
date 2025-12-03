package cn.loblok.upc.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@Getter
@Setter
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;


    @TableField("exp")
    private Integer exp;

    @TableField("points")
    private Integer points;

    @TableField("user_level")
    private String userLevel;
    /**
     * 是否为永久会员 (0: 否, 1: 是)
     */
    @TableField("is_permanent_member")
    private Boolean  isPermanentMember = false;

    /**
     * 会员过期时间（null 表示非会员 或 永久会员）
     */
    @TableField("member_expire_at")
    private LocalDateTime memberExpireAt;


    @TableField("tenant_id")
    private String tenantId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}