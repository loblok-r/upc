package cn.loblok.upc.community.dto;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import lombok.Data;

@Data
public class CreatorLeaderboardResponse {

    int rank;

    int score;

    UserPublicInfoDTO author;
}