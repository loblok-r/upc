package cn.loblok.upc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *      每日额度使用表
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
@Getter
@Setter
@TableName("daily_usage")
public class DailyUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("date")
    private LocalDate date;

    @TableField("text_chat_count")
    private Integer textChatCount;

    @TableField("ai_drawing_count")
    private Integer aiDrawingCount;
}
