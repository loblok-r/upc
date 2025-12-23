package cn.loblok.upc.auth.entity;

import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.enums.UserItemType;
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
 * 用户虚拟道具/资产表（不含优惠券）
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
@Getter
@Setter
@TableName("user_items")
public class UserItems implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 道具类型，约定命名规范，如：
     1, "compute_power", // 算力
     2, "vip", // VIP会员
     3, "resign_card", // 补签卡
     4, "lottery_ticket", // 抽奖次数
     5, "cash_bonus", // 现金奖金
     6, "ai_drawing_quota", // AI绘图配额
     7, "annual_membership_voucher", // 年度会员兑换码
     8, "discount_coupon"; // 优惠券
     */
    @TableField("item_type")
    private UserItemType itemType;

    /**
     * 当前数量，必须 >= 0
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 生效时间
     */
    @TableField("valid_from")
    private LocalDateTime validFrom;

    /**
     * 过期时间
     */
    @TableField("valid_to")
    private LocalDateTime validTo;

    /**
     * 来源类型：
        1.flash_sale（积分秒杀）
        2.points_trading（积分兑换）
        3.daily_task（每日任务）
        4.event_reward（活动奖励）
        5.admin_grant（运营发放）
     */
    @TableField("source_type")
    private UserItemSourceType sourceType;

    /**
     * 来源唯一ID，如：
        秒杀订单ID（flash_order_123）
        任务ID（task_456）
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 扩展信息，例如：
        {"max_usable_per_day": 1} 
        {"event_id": "summer2025"}
     */
    @TableField("extra")
    private String extra;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
