package cn.loblok.upc.api.user.dto;

import lombok.Data;

/**
 * 社交名片（他人可见）
 */
@Data
public class UserPublicInfoDTO {

    private Long id;

    private String name;

    private String avatar;


    private String handle;

    private int followers;

    private Boolean isVerified;


    private Boolean isMember; // 是否是 PRO 会员

    private String bio; // 用户简介

    // 个人中心统计数据 (可选，视后端返回情况)
    private UserStatsDTO stats;

}