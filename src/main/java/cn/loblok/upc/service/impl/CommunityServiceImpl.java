package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.CreatorLeaderboardResponse;
import cn.loblok.upc.dto.LeaderboardAuthor;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.service.CommunityService;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@AllArgsConstructor
public class CommunityServiceImpl implements CommunityService {


    private final UserService userService;

    private final FollowService followService;
    /**
     * 获取创作者排行榜
     *
     * @return
     */
    @Override
    public List<CreatorLeaderboardResponse> getCreatorLeaderboard(Long userId) {
        // 获取所有用户
        List<User> allUsers = userService.list();

        List<CreatorLeaderboardResponse> creatorLeaderboardResponses = getCreatorLeaderboardResponses(userId, allUsers);

        return creatorLeaderboardResponses;
    }


    @Override
    public List<CreatorLeaderboardResponse> getNewCreatorLeaderboard(Long userId) {
        // 获取30天内注册的用户
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<User> recentUsers = userService.lambdaQuery()
                .ge(User::getCreatedAt, thirtyDaysAgo)
                .list();

        List<CreatorLeaderboardResponse> result = getCreatorLeaderboardResponses(userId, recentUsers);

        return result;
    }

    @NotNull
    private List<CreatorLeaderboardResponse> getCreatorLeaderboardResponses(Long userId, List<User> recentUsers) {
        // 按照粉丝数和点赞数6:4权重排序，取前10名
        List<User> sortedUsers = recentUsers.stream()
                .sorted((user1, user2) -> {
                    // 计算加权分数：粉丝数*0.6 + 获赞数*0.4
                    double score1 = user1.getFollowers() * 0.6 + user1.getLikes() * 0.4;
                    double score2 = user2.getFollowers() * 0.6 + user2.getLikes() * 0.4;
                    // 降序排列（分数高的在前）
                    return Double.compare(score2, score1);
                })
                .limit(10) // 取前10名
                .collect(Collectors.toList());

        // 封装结果
        List<CreatorLeaderboardResponse> result = new ArrayList<>();
        for (int i = 0; i < sortedUsers.size(); i++) {
            User user = sortedUsers.get(i);
            //查看是否关注
            boolean followed =followService.isFollowed(userId, user.getId());

            // 计算分数
            double score = user.getFollowers() * 0.6 + user.getLikes() * 0.4;

            // 将User对象转换为Author对象
            LeaderboardAuthor author = new LeaderboardAuthor();
            author.setId(user.getId());
            author.setName(user.getUsername());
            author.setAvatar(user.getAvatarUrl());
            author.setHandle(user.getUsername());
            author.setFollowers(user.getFollowers());
            author.setVerified(false);
            author.setFollowed(followed);
            CreatorLeaderboardResponse response = new CreatorLeaderboardResponse();
            response.setScore((int)score);
            response.setRank(i + 1); // 排名从1开始
            response.setAuthor(author);

            result.add(response);
        }
        return result;
    }
}