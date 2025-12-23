package cn.loblok.upc.trade.entity;

import cn.loblok.upc.common.enums.MallOrderStatus;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *       秒杀订单
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Getter
@Setter
@TableName("orders")
public class FlashSaleOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("user_id")
    private Long userId;

    @TableField(value = "flash_sale_id",insertStrategy = FieldStrategy.ALWAYS)
    private String flashSaleId;

    @TableField("product_id")
    private String productId;

    @TableField("points_spent")
    private Integer pointsSpent;

    @TableField("order_status")
    private MallOrderStatus mallOrderStatus;

    @TableField("reserve_expires_at")
    private LocalDateTime reserveExpiresAt;

    @TableField("transaction_id")
    private Long transactionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
