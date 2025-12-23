package cn.loblok.upc.auth.entity;

import cn.loblok.upc.common.enums.MessageContentType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * AI 生成请求日志表
 * </p>
 *
 * @author loblok
 * @since 2025-12-12
 */
@Getter
@Setter
@TableName("ai_generation_logs")
public class AiGenerationLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * UUID 字符串，唯一主键
     */
    @TableId("id")
    private String id;

    /**
     * 关联用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话 ID，用于前端历史记录分组
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 用户输入的提示词
     */
    @TableField("prompt")
    private String prompt;

    /**
     * 生成参数（模型版本、尺寸、步数、参考图 URL 等）
     */
    @TableField("params")
    private String params;

    /**
     * 本次消耗的算力值
     */
    @TableField("cost")
    private Integer cost;

    /**
     * 任务状态
     */
    @TableField("status")
    private String status;

    /**
     * 生成图片的 OSS/S3 地址（仅图片生成时有值）
     */
    @TableField("result_url")
    private String resultUrl;

    /**
     * 失败时的错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 创建时间，毫秒精度
     */
    @TableField("created_at")
    private LocalDateTime createdAt;


    @TableField("content_type")
    private MessageContentType contentType;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private  Boolean isDeleted;

    /**
     * 是否置顶
     */
    @TableField("is_pinned")
    private  Boolean isPinned;
}
