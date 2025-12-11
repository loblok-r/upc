package cn.loblok.upc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LeaderboardAuthor {
    private Long id;

    private String name;

    private String avatar;


    private String handle;

    @JsonProperty("isVerified")
    private boolean isVerified;

    private int followers;

    @JsonProperty("isFollowed")
    private boolean isFollowed;
}