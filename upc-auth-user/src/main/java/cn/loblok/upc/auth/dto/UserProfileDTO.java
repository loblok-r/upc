package cn.loblok.upc.auth.dto;

import cn.loblok.upc.api.user.dto.UserStatsDTO;
import cn.loblok.upc.api.ai.dto.DailyUsageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 用户资料返回对象
 */
@Data
public class UserProfileDTO {
@Schema(description = "用户资料返回对象")
    private Long userId;

    /**
     * 返回的用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 返回的邮箱
     */
    @Schema(description = "邮箱")
    private String email;


    /**
     * 返回的头像
     */
    @Schema(description = "头像")
    private  String avatar; //"avatar": "https://example.com/avatar.jpg"


    /**
     * 返回的会员状态
     */
    @Schema(description = "会员状态")
    private boolean permanentMember;

    /**
     * 返回的会员到期时间
     */
    @Schema(description = "会员到期时间")
    private LocalDateTime memberExpireAt;

    /**
     * 返回的经验值
     */
    @Schema(description = "经验值")
    private int exp;

    /**
     * 返回的抽奖次数
     */
    @Schema(description = "抽奖次数")
    private int lotteryCounts;

    /**
     * 返回的连击天数
     */
    @Schema(description = "连击天数")
    private int streakDays;

    /**
     *  返回的签到状态
     */
    @Schema(description = "签到状态")
    private boolean checkedIn;

    /**
     * 返回的积分
     */
    @Schema(description = "积分")
    private int points;

    /**
     * 返回的等级
     */
    @Schema(description = "等级")
    private String userLevel;

    /**
     * 返回的签到历史
    *
     **/
    @Schema(description = "签到历史")
    private String[] checkInHistory;


    /**
     * 返回的算力
     */
    @Schema(description = "算力")
    private int computingPower;

    /**
     * 返回的统计数据
     */
    @Schema(description = "统计数据")
    private UserStatsDTO stats;


    /**
     * 24小时使用情况
     */
    @Schema(description = "24小时使用情况")
    private DailyUsageResponse dailyUsage;

    /**
     * 补签次数
     */
    @Schema(description = "补签次数")
    private int retroCounts;

}