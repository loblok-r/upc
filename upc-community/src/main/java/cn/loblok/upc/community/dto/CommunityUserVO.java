package cn.loblok.upc.community.dto;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import lombok.Data;

@Data
public class CommunityUserVO extends UserPublicInfoDTO {
    private Boolean isFollowed;
}