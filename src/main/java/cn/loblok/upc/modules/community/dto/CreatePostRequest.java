package cn.loblok.upc.modules.community.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    String title;
    String content;
    String imageUrl;
    int width;
    int height;

    public String getSize(){
        return width + "x" + height;
    }
}