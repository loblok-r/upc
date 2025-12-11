package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    String title;
    String content;
    String imageUrl;
}