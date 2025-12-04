package cn.loblok.upc.entity;

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

    @TableField("email")
    private String email; // 用于登录和找回密码（比 username 更通用）

    @TableField("phone")
    private String phone; // 可选，如果未来支持短信登录

    @TableField("avatar_url")
    private String avatarUrl; // 存储用户头像（可先为空）

    // ✅ 安全相关（必须）
    @TableField("salt")
    private String salt; // 密码加盐，提升安全性

    @TableField("status")
    private Integer status; // 0: 正常, 1: 冻结, 2: 未激活（防垃圾注册）

    // ✅ 时间字段补充
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt; // 记录最后登录时间


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