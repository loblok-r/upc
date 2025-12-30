package cn.loblok.upc.community.dto;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreatorLeaderboardResponse {

    @Schema(description = "排名")
    int rank;

    @Schema(description = "得分")
    int score;

    @Schema(description = "作者信息")
    UserPublicInfoDTO author;
}