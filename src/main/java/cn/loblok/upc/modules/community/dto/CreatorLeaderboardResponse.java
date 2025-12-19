package cn.loblok.upc.modules.community.dto;

import lombok.Data;

@Data
public class CreatorLeaderboardResponse {

    int rank;

    int score;

    LeaderboardAuthor author;
}