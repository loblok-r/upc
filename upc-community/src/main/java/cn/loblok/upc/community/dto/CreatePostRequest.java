package cn.loblok.upc.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreatePostRequest {
    @Schema(description = "标题")
    String title;
    @Schema(description = "内容")
    String content;
    @Schema(description = "图片地址")
    String imageUrl;
    @Schema(description = "图片宽")
    int width;
    @Schema(description = "图片高")
    int height;


    public String getSize(){
        return width + "x" + height;
    }
}