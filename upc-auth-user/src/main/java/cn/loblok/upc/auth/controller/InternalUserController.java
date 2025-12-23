package cn.loblok.upc.auth.controller;

import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.auth.service.PointsService;
import cn.loblok.upc.auth.service.UserItemsService;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.auth.service.UserService;

import cn.loblok.upc.common.enums.AppMode;
import cn.loblok.upc.common.enums.UserItemSourceType;
import cn.loblok.upc.common.enums.UserItemType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/internal")
@Slf4j
@AllArgsConstructor
public class InternalUserController {


    private final UserService userService;

    private final PointsService pointsService;

    private final UserItemsService userItemsService;

    @PostMapping("/info/user")
    public Result<UserPublicInfoDTO> getUserInfo(@RequestParam(value = "userId")  Long userId) {

        return userService.getUserPublicInfo(userId);
    }

    @PostMapping("/points/update")
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
    Result<Map<Long, UserPublicInfoDTO>> getUserPublicInfoBatch(@RequestBody List<Long> userIds) {


        return userService.getUserPublicInfoBatch(userIds);
    }


    @PostMapping("/followingsCounts/update")
    void updateFollowingsCounts(Long userId, Integer delta) {
        userService.updateFollowingsCounts(userId,delta);

        log.info("更新用户关注数成功");
    }

    @PostMapping("/followersCounts/update")
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
    public Result<List<UserPublicInfoDTO>> getRecommendedUsers(@RequestParam("limit") Integer limit, @RequestBody List<Long> excludeIds) {


        return userService.getRecommendedUsers(limit, excludeIds);

    }


    /**
     * 权限预检：检查余额和日限额
     */
    @GetMapping("/access/check")
    Result<Void> checkAiAccess(@RequestParam("userId") Long userId,
                                  @RequestParam("mode") AppMode mode,
                                  @RequestParam("amount") Integer amount){
        return userService.checkAiAccess(userId, mode, amount);
    }


    /**
     * 算力扣减
     */
    @PostMapping("/computePower/consume")
    Result<Void> consumeComputerPower(@RequestParam("userId") Long userId,
                               @RequestParam("amount") Integer amount){
        return userService.consumeComputerPower(userId, amount);
    }

    /**
     * 更新算力
     */
    @PostMapping("/computePower/add")
    public Result<Boolean> addComputingPower(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) {
        return userService.addComputingPower(userId, amount);
    }


    /**
     * 增加积分
     */
    @PostMapping("/userPoints/add")
    Result<Void> addPoints(@RequestParam("userId") Long userId, @RequestParam("amount") Integer amount) {
        return pointsService.addUserPoints(userId, amount);
    }

    /**
     * 积分扣减
     */
    @PostMapping("/userPoints/reduce")
    Result<Void> reduceUserPoints(@RequestParam("userId")Long userId, @RequestParam("amount")Integer amount){
        return pointsService.reduceUserPoints(userId, amount);
    }


    /**
     * 添加用户道具
     */
    @PostMapping("/userItems/add")
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
    Result<Void>  extendVipDays(@RequestParam("userId")Long userId, @RequestParam("days")Integer days){
         return userService.extendVipDays(userId, days);
    }

    /**
     * 抽奖次数扣减
     */
    @PostMapping("/items/consume-lottery")
    Result<Integer> consumeLotteryTicket(@RequestParam("userId") Long userId){
        return userService.consumeLotteryTicket(userId);
    }



}