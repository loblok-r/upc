package cn.loblok.upc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthResponseDTO {

    /**
     * 登录成功返回的token
     */

    private String token;

    /**
     * 登录成功返回的用户名
     */
    private String username;

    /**
     * 登录成功返回的邮箱
     */
    private String email;

    /**
     * 登录成功返回的用户id
     */
    private Long userId;

    /**
     * 登录成功返回的头像
     */
    private  String avatar; //"avatar": "https://example.com/avatar.jpg"


    /**
     * 登录成功返回的会员状态
     */
    private boolean isPermanentMember;

    /**
     * 登录成功返回的会员到期时间
     */
    private LocalDateTime memberExpireAt;

    /**
     * 登录成功返回的token过期时间
     */
    private long expiresIn;


    /**
     * 登录成功返回的经验值
     */
    private int exp;

    /**
     * 登录成功返回的抽奖次数
     */
    private int lotteryCounts;

    /**
     * 登录成功返回的连击天数
     */
    private int streakDays;

    /**
     *  登录成功返回的签到状态
     */
    private boolean isCheckedIn;

    /**
     * 登录成功返回的积分
     */
    private int points;



    /**
     * 登录成功返回的等级
     */
    private String userLevel;


    /**
     * 登录成功返回的算力
     */
    private int computingPower;

    /**
     * 登录成功返回的统计数据
     */
    private StatsData stats;

}