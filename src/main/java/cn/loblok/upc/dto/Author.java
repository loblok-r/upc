package cn.loblok.upc.dto;

import lombok.Data;

/**
 * 作者信息
 */
@Data
public class Author {

    private Long id;

    private String name;

    private String avatar;


    private String handle;

    private int followers;

    private Boolean isVerified;

    private Boolean isFollowed;

    // 个人中心统计数据 (可选，视后端返回情况)
    private StatsData stats;

    private Integer computingPower;

    private Integer maxcomputingPower;

    private Boolean isMember; // 是否是 PRO 会员

    private String bio; // 用户简介

}