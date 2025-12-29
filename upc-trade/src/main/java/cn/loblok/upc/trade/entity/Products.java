package cn.loblok.upc.trade.entity;

import cn.loblok.upc.trade.enums.ProductStatus;
import cn.loblok.upc.common.enums.ProductType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *    产品表
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Getter
@Setter
@TableName("products")
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("name")
    private String name;

    /**
     * 是否抽奖产品
     */

    @TableField("lottery_eligible")
    private Boolean lotteryEligible;

    /**
     * 图标名称
     */

    @TableField("icon_name")
    private String iconName;

    /**
     * 颜色 抽奖的产品贵重程度 黄色 > 紫色 > 蓝色
     */
    @TableField("display_color")
    private String displayColor;

    @TableField("description")
    private String description;

    /**
     * 分类
     *
     * 1. 实物商品	physical 需要物流发货的实体物品	订单进入「待发货」，人工/系统后续处理
     * 2. 虚拟权益	virtual	直接提升用户账户数值或状态（如算力、会员）	同步发放（写 DB）
     * 3. 优惠凭证	voucher 一种“未来可用”的权利（如折扣、卡券、抽奖次数）	同步发放到用户资产表，包含一些 可消耗的道具
     *
     */
    @TableField("category")
    private ProductType category;

    @TableField("points_required")
    private Integer pointsRequired;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("limit_per_user")
    private Integer limitPerUser;

    @TableField("tag")
    private String tag;

    @TableField("image_url")
    private String imageUrl;

    @TableField("status")
    private ProductStatus status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("delivery_config")
    private String deliveryConfig;

    @Version
    @TableField("version")
    private Integer version;

}
