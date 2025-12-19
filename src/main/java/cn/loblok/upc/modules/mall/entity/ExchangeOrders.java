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
 *    交易订单
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Getter
@Setter
@TableName("exchange_orders")
public class ExchangeOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private String productId;

    @TableField("product_name")
    private String productName;

    @TableField("points_spent")
    private Integer pointsSpent;

    @TableField("quantity")
    private Integer quantity;

    @TableField("order_status")
    private String orderStatus;

    @TableField("shipping_info")
    private String shippingInfo;

    @TableField("is_virtual")
    private Boolean isVirtual;

    @TableField("virtual_content")
    private String virtualContent;

    @TableField("transaction_id")
    private Long transactionId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
