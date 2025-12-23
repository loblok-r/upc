package cn.loblok.upc.community.dto;

import lombok.Data;

@Data
public class LeaderboardData {
    int rank;

    int score;

    Long userId;
}