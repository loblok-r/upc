package cn.loblok.upc.worker.service;

import cn.loblok.upc.api.user.feign.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatService {

    @Autowired
    private UserFeignClient userFeignClient;

    public void updateUserLikes(Long userId, Integer delta) {
        userFeignClient.updateLikeCounts(userId, delta);
    }

    public void updateUserWorks(Long userId, Integer delta) {
        userFeignClient.updateUserWorkCounts(userId, delta);
    }

    public void updateUserFollowers(Long userId,Integer delta) {
        userFeignClient.updateFollowersCounts(userId, delta);
    }

    public void updateUserFollowings(Long userId, Integer delta) {

        userFeignClient.updateFollowingsCounts(userId, delta);
    }
}