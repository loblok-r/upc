package cn.loblok.upc.api.user.feign;


import cn.loblok.upc.api.constant.ServiceNameConstants;
import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.enums.UserItemType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = ServiceNameConstants.AUTH_SERVICE, path = "/api/user/internal")
public interface UserFeignClient {

    @PostMapping("/info/user")
    Result<UserPublicInfoDTO> getUserInfo(@RequestParam(value = "userId") Long userId);
    /**
     * 检查用户是否是会员
     *
     * @param userId 用户ID
     * @return 是否是会员
     */
    @PostMapping("/member/check")
    Result<Boolean> checkMemberStatus(@RequestParam("userId") Long userId);


    /**
     * 给社区服务用的：批量获取用户信息，用于展示帖子列表
     */
    @GetMapping("/public/list")
    Result<Map<Long, UserPublicInfoDTO>> getUserPublicInfoBatch(@RequestBody List<Long> userIds);

    /**
     * 批量更新用户粉丝数
     *
     * @param userId 用户 ID
     * @param delta  粉丝数变化量
     */

    @PostMapping("/followersCounts/update")
    void updateFollowersCounts(@RequestParam("userId")Long userId,@RequestParam("delta") Integer delta);

    /**
     * 批量更新用户关注数
     *
     * @param userId 用户 ID
     * @param delta  关注数变化量
     */

    @PostMapping("/followingsCounts/update")
    void updateFollowingsCounts(@RequestParam("userId")Long userId, @RequestParam("delta")Integer delta);

    /**
     * 更新用户作品数
     *
     * @param userId 用户 ID
     * @param delta  增加的数值
     */
    @PostMapping("/workCounts/update")
    void updateUserWorkCounts(@RequestParam("userId")Long userId, @RequestParam("delta")Integer delta);

    /**
     * 更新用户点赞数
     *
     * @param userId 用户 ID
     * @param delta  增加的数值
     */
    @PostMapping("/likeCounts/update")
    void updateLikeCounts(@RequestParam("userId")Long userId, @RequestParam("delta")Integer delta);


    /**
     * 获取推荐用户
     *
     * @param limit 返回数量限制
     * @param
     * @return 推荐用户列表
     */
    @PostMapping("/recommend")
    Result<List<UserPublicInfoDTO>> getRecommendedUsers(
            @RequestParam("limit") Integer limit,
            @RequestBody List<Long> excludeUserIds // 传入当前用户已经关注的 ID 列表
    );


    /**
     * 权限与余额预检
     * @param userId 用户ID
     * @param mode 模式（用于校验日限额）
     * @param amount 本次预计消耗的算力值
     */
    @GetMapping("/access/check")
    Result<Void> checkAiAccess(@RequestParam("userId") Long userId,
                                  @RequestParam("mode") AppMode mode,
                                  @RequestParam("amount") Integer amount);

    /**
     * 算力扣减
     */
    @PostMapping("/computePower/consume")
    Result<Void> consumeComputerPower(@RequestParam("userId") Long userId,
                               @RequestParam("amount") Integer amount);

    /**
     * 增加算力
     */
    @PostMapping("/computePower/add")
    Result<Void> addComputingPower(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) ;


    /**
     * 增加积分
     */
    @PostMapping("/userPoints/add")
    Result<Void> addPoints(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) ;


    /**
     * 积分扣减
     */
    @PostMapping("/userPoints/reduce")
    Result<Void> reduceUserPoints(@RequestParam("userId")Long userId, @RequestParam("amount")Integer amount);

    /**
     * 添加用户道具
     */
    @PostMapping("/userItems/add")
    Result<Void>  addUserItem(@RequestParam("userId")Long userId,
                              @RequestParam("itemType") UserItemType itemType,
                              @RequestParam("sourceType") UserItemSourceType sourceType,
                              @RequestParam("sourceId")String sourceId,
                              @RequestParam("extra")Map<String, Object> extra,
                              @RequestParam("counts")int counts);

    /**
     * 延长会员天数
     * */
    @PostMapping("/member/extend")
    Result<Void>  extendVipDays(@RequestParam("userId")Long userId, @RequestParam("days")Integer days);


    /**
     * 抽奖次数扣减
     */
    @PostMapping("/items/consume-lottery")
    Result<Integer> consumeLotteryTicket(@RequestParam("userId") Long userId);
}



