package cn.loblok.upc.community.dto;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommunityUserVO extends UserPublicInfoDTO {
    @Schema(description = "是否已关注")
    private Boolean isFollowed;
}