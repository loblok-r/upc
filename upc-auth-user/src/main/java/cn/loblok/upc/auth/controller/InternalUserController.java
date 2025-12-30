package cn.loblok.upc.auth.controller;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.auth.service.PointsService;
import cn.loblok.upc.auth.service.UserItemsService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.service.UserService;

import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.enums.UserItemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/internal")
@Slf4j
@AllArgsConstructor
@Tag(name = "内部用户接口", description = "内部用户接口")
public class InternalUserController {


    private final UserService userService;

    private final PointsService pointsService;

    private final UserItemsService userItemsService;

    @PostMapping("/info/user")
    @Operation(summary = "获取用户信息")
    public Result<UserPublicInfoDTO> getUserInfo(@RequestParam(value = "userId")  Long userId) {

        return userService.getUserPublicInfo(userId);
    }

    @PostMapping("/points/update")
    @Operation(summary = "更新用户积分")
    public Result<Boolean> updatePoints(@RequestParam("userId") Long userId, @RequestParam("delta") Integer delta) {
        // todo 修改积分逻辑
        return null;
    }

    /**
     * 获取用户会员状态
     *
     * @param userId
     * @return
     */

    @PostMapping("/member/check")
    @Operation(summary = "获取用户会员状态")
    Result<Boolean> checkMemberStatus(@RequestParam("userId") Long userId) {

        Boolean result = userService.isMember(userId);
        return Result.success(result);
    }

    /**
     * 批量获取用户信息
     *
     * @param userIds
     * @return
     */
    @PostMapping("/public/list")
    @Operation(summary = "批量获取用户信息")
    Result<Map<Long, UserPublicInfoDTO>> getUserPublicInfoBatch(@RequestBody List<Long> userIds) {


        return userService.getUserPublicInfoBatch(userIds);
    }


    @PostMapping("/followingsCounts/update")
    @Operation(summary = "更新用户关注数")
    void updateFollowingsCounts(Long userId, Integer delta) {
        userService.updateFollowingsCounts(userId,delta);

        log.info("更新用户关注数成功");
    }

    @PostMapping("/followersCounts/update")
    @Operation(summary = "更新用户粉丝数")
    void updateFollowersCounts(@RequestParam("userId") Long userId, @RequestParam("delta") Integer delta) {
        userService.updateFollowersCounts(userId, delta);

        log.info("更新用户作品数成功");
    }

    /**
     * 更新用户作品数
     *
     * @param userId
     * @param delta
     */
    @PostMapping("/workCounts/update")
    @Operation(summary = "更新用户作品数")
    void updateUserWorkCounts(@RequestParam("userId") Long userId,@RequestParam("delta") Integer delta) {
        userService.updateUserWorkCounts(userId, delta);

        log.info("更新用户作品数成功");
    }

    /**
     * 更新用户点赞数
     *
     * @param userId
     * @param delta
     */
    @PostMapping("/likeCounts/update")
    @Operation(summary = "更新用户点赞数")
    void updateLikeCounts(@RequestParam("userId") Long userId, @RequestParam("delta") Integer delta){
        userService.updateLikeCounts(userId, delta);
        log.info("更新用户点赞数成功");
    }
    /**
     * 获取推荐用户
     *
     * @param limit
     * @param excludeIds
     * @return
     */
    @PostMapping("/recommend")
    @Operation(summary = "获取推荐用户")
    public Result<List<UserPublicInfoDTO>> getRecommendedUsers(@RequestParam("limit") Integer limit, @RequestBody List<Long> excludeIds) {


        return userService.getRecommendedUsers(limit, excludeIds);

    }


    /**
     * 权限预检：检查余额和日限额
     */
    @GetMapping("/access/check")
    @Operation(summary = "权限预检：检查余额和日限额")
    Result<Void> checkAiAccess(@RequestParam("userId") Long userId,
                                  @RequestParam("mode") AppMode mode,
                                  @RequestParam("amount") Integer amount){
        return userService.checkAiAccess(userId, mode, amount);
    }


    /**
     * 算力扣减
     */
    @PostMapping("/computePower/consume")
    @Operation(summary = "算力扣减")
    Result<Void> consumeComputerPower(@RequestParam("userId") Long userId,
                               @RequestParam("amount") Integer amount){
        return userService.consumeComputerPower(userId, amount);
    }

    /**
     * 更新算力
     */
    @PostMapping("/computePower/add")
    @Operation(summary = "更新算力")
    public Result<Boolean> addComputingPower(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) {
        return userService.addComputingPower(userId, amount);
    }


    /**
     * 增加积分
     */
    @PostMapping("/userPoints/add")
    @Operation(summary = "增加积分")
    Result<Void> addPoints(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) {
        return pointsService.addUserPoints(userId, amount);
    }

    /**
     * 积分扣减
     */
    @PostMapping("/userPoints/reduce")
    @Operation(summary = "积分扣减")
    Result<Void> reduceUserPoints(@RequestParam("userId")Long userId, @RequestParam("amount")Integer amount){
        return pointsService.reduceUserPoints(userId, amount);
    }


    /**
     * 添加用户道具
     */
    @PostMapping("/userItems/add")
    @Operation(summary = "添加用户道具")
    Result<Void>  addUserItem(@RequestParam("userId")Long userId,
                              @RequestParam("itemType")UserItemType itemType,
                               @RequestParam("sourceType")UserItemSourceType sourceType,
                              @RequestParam("sourceId")String sourceId,
                              @RequestParam("extra")Map<String, Object> extra,
                              @RequestParam("counts")int counts){
        return userItemsService.addItem(userId, itemType, sourceType, sourceId, extra, counts);
    }

    /**
     * 延长会员天数
     * */
    @PostMapping("/member/extend")
    @Operation(summary = "延长会员天数")
    Result<Void>  extendVipDays(@RequestParam("userId")Long userId, @RequestParam("days")Integer days){
         return userService.extendVipDays(userId, days);
    }

    /**
     * 抽奖次数扣减
     */
    @PostMapping("/items/consume-lottery")
    @Operation(summary = "抽奖次数扣减")
    Result<Integer> consumeLotteryTicket(@RequestParam("userId") Long userId){
        return userService.consumeLotteryTicket(userId);
    }



}