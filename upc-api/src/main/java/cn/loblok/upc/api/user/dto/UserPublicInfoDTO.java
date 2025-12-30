package cn.loblok.upc.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 社交名片（他人可见）
 */
@Data
public class UserPublicInfoDTO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String name;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;


    @Schema(description = "handle")
    private String handle;

    /**
     * 粉丝数
     */
    @Schema(description = "粉丝数")
    private int followers;

    /**
     * isVerified
     */
    @Schema(description = "是否是 verified")
    private Boolean isVerified;


    /**
     * 是否是会员
     */
    @Schema(description = "是否是会员")
    private Boolean isMember; // 是否是 PRO 会员

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String bio; // 用户简介

    /**
     * 社交信息
     */
    @Schema(description = "社交信息")
    // 个人中心统计数据 (可选，视后端返回情况)
    private UserStatsDTO stats;

}