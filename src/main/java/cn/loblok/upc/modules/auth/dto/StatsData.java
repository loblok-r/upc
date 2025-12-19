package cn.loblok.upc.modules.auth.dto;

import lombok.Data;

/**
 * 用户社区状态
 *
 */
@Data
public class StatsData {

    private int works;

    private int followers;

    private int following;

    private int likes;
}