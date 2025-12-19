package cn.loblok.upc.modules.auth.entity;

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
 *      用户表
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
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("avatar_url")
    private String avatarUrl;


    /**
     * 密码盐
     */
    @TableField("salt")
    private String salt;

    /**
     * 状态 0: 正常, 1: 冻结, 2: 未激活（防垃圾注册）
     */
    @TableField("status")
    private Integer status;


    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("works")
    private Integer works;

    @TableField("likes")
    private Integer likes;

    @TableField("followers")
    private Integer followers;


    @TableField("followings")
    private Integer followings;

    @TableField("lottery_counts")
    private Integer lotteryCounts;

    @TableField("exp")
    private Integer exp;

    @TableField("points")
    private Integer points;

    @TableField("computing_power")
    private Integer computingPower;

    @TableField("user_level")
    private String userLevel;

    /**
     * 当天是否签到 (0: 否, 1: 是)
     */
    @TableField("ischickined")
    private Boolean  ischickined = false;


    /**
     * 连续签到天数
     */
    @TableField("streakdays")
    private Integer streakdays;
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

    @TableField("update_at")
    private LocalDateTime updateAt;
}