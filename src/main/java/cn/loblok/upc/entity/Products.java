package cn.loblok.upc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
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

    @TableField("category")
    private String category;

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
    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
