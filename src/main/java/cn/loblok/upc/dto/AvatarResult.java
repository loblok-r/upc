package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class AvatarResult {
    private String taskId;
    private String imageUrl;      // 生成的头像 CDN 地址
    private String thumbnailUrl;  // 缩略图
    private boolean commercialAllowed; // 是否可商用（仅 PRO=true）
    private String message;       // 调试信息，如 "Mock: BASIC generated"

    // 手动覆盖 setter，支持链式调用
    public AvatarResult setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public AvatarResult setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public AvatarResult setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public AvatarResult setCommercialAllowed(boolean commercialAllowed) {
        this.commercialAllowed = commercialAllowed;
        return this;
    }

    public AvatarResult setMessage(String message) {
        this.message = message;
        return this;
    }
}