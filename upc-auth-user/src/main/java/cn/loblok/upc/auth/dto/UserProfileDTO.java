package cn.loblok.upc.auth.dto;

import cn.loblok.upc.api.user.dto.UserStatsDTO;
import cn.loblok.upc.api.ai.dto.DailyUsageResponse;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 用户资料返回对象
 */
@Data
public class UserProfileDTO {

    private Long userId;

    /**
     * 返回的用户名
     */
    private String username;

    /**
     * 返回的邮箱
     */
    private String email;


    /**
     * 返回的头像
     */
    private  String avatar; //"avatar": "https://example.com/avatar.jpg"


    /**
     * 返回的会员状态
     */
    private boolean permanentMember;

    /**
     * 返回的会员到期时间
     */
    private LocalDateTime memberExpireAt;

    /**
     * 返回的经验值
     */
    private int exp;

    /**
     * 返回的抽奖次数
     */
    private int lotteryCounts;

    /**
     * 返回的连击天数
     */
    private int streakDays;

    /**
     *  返回的签到状态
     */
    private boolean checkedIn;

    /**
     * 返回的积分
     */
    private int points;

    /**
     * 返回的等级
     */
    private String userLevel;

    /**
     * 返回的签到历史
    *
     **/
    private String[] checkInHistory;


    /**
     * 返回的算力
     */
    private int computingPower;

    /**
     * 返回的统计数据
     */
    private UserStatsDTO stats;


    private DailyUsageResponse dailyUsage;

    /**
     * 补签次数
     */
    private int retroCounts;

}