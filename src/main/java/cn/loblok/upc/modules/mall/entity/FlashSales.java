package cn.loblok.upc.modules.mall.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *       秒杀商品
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Getter
@Setter
@TableName("flash_sales")
public class FlashSales implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("product_id")
    private String productId;

    @TableField("sale_price")
    private Integer salePrice;

    @TableField("original_price")
    private Integer originalPrice;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("remaining_stock")
    private Integer remainingStock;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("daily_limit")
    private Integer dailyLimit;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
