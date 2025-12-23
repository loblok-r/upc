package cn.loblok.upc.api.user.dto;

import lombok.Data;

/**
 * 用户社区状态
 *
 */
@Data
public class UserStatsDTO {

    private int works;
    private int likes;


    // 来自User 模块
    private int followers;
    private int following;
}