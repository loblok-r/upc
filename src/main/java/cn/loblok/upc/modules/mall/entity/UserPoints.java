package cn.loblok.upc.modules.mall.entity;

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
 *      用户积分表
 * </p>
 *
 * @author loblok
 * @since 2025-12-09
 */
@Getter
@Setter
@TableName("user_points")
public class UserPoints implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 当前余额
     */
    @TableField("balance")
    private Integer balance;

    /**
     * 总获得积分
     */
    @TableField("total_earned")
    private Integer totalEarned;

    /**
     * 总消费积分
     */
    @TableField("total_spent")
    private Integer totalSpent;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
