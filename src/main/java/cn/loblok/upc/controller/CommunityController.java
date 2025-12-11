package cn.loblok.upc.controller;


import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.FollowUserRequest;
import cn.loblok.upc.dto.FollowUserResponse;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.entity.Follow;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private FollowService followService;
    
    @Autowired
    private UserService userService;

    /**
     * 关注或取消关注用户
     * 如果已经关注则取消关注，如果未关注则进行关注
     *
     * @param userId 当前用户ID
     * @param request 包含目标用户ID的请求体
     * @return 操作结果
     */
    @PostMapping("/follow")
    public Result<FollowUserResponse> followOrUnfollow(@CurrentUser Long userId, @RequestBody FollowUserRequest request) {
        try {
            FollowUserResponse response = followService.followOrUnfollow(userId, request.getTargetUserId());
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(500, "操作失败", e.getMessage());
        }
    }
    
    /**
     * 查询当前用户关注的用户列表
     *
     * @param userId 当前用户ID
     * @return 关注的用户列表
     */
    @GetMapping("/following")
    public Result<List<User>> getFollowingList(@CurrentUser Long userId) {
        try {
            List<User> followingUsers = followService.getFollowingList(userId);
            return Result.success(followingUsers);
        } catch (Exception e) {
            return Result.error(500, "查询失败", e.getMessage());
        }
    }

}