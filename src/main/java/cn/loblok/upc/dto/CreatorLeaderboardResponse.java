package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class CreatorLeaderboardResponse {

    int rank;

    int score;

    LeaderboardAuthor author;
}