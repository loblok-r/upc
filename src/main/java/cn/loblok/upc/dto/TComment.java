package cn.loblok.upc.dto;

import cn.loblok.upc.entity.User;
import lombok.Data;

/**
 * 评论响应
 */

@Data
public class TComment {

    // 评论ID
    private String id;

    // 用户ID
    private String userId;

    // 用户信息
    private User user;

    // 评论内容
    private String text;

    // 点赞数
    private Integer likes;

    // 当前用户是否点赞了该评论
    private Boolean isLiked;

    // ISO String
    private String createdAt;

    //二级回复
    private TComment[] replies;

    // 前端计算的时间
    private String timeAgo;

}