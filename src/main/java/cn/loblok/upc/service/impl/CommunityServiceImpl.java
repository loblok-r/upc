package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.*;
import cn.loblok.upc.entity.Comment;
import cn.loblok.upc.entity.LikeRecord;
import cn.loblok.upc.entity.Posts;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.entity.UserSearchResult;
import cn.loblok.upc.mapper.CommentMapper;
import cn.loblok.upc.mapper.LikeRecordMapper;
import cn.loblok.upc.mapper.PostsMapper;
import cn.loblok.upc.mapper.UserMapper;
import cn.loblok.upc.service.CommunityService;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.UserService;
import cn.loblok.upc.util.TencentCOSUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    
    private final UserMapper userMapper;

    private final FollowService followService;
    
    private final CommentMapper commentMapper;
    
    private final LikeRecordMapper likeRecordMapper;
    
    private final PostsMapper postsMapper;

    private final TencentCOSUtil tencentCOSUtil;

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

    @Override
    public String likeOrUnlikeComment(Long userId, Long commentId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new RuntimeException("评论不存在或已被删除");
        }

        // 检查是否已经点赞
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("target_type", "comment");
        queryWrapper.eq("target_id", commentId);
        
        LikeRecord existingLike = likeRecordMapper.selectOne(queryWrapper);
        
        if (existingLike != null) {
            // 已经点赞，执行取消点赞操作
            likeRecordMapper.deleteById(existingLike.getId());
            
            // 减少评论的点赞数
            comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
            commentMapper.updateById(comment);
            
            return "取消点赞成功";
        } else {
            // 未点赞，执行点赞操作
            LikeRecord likeRecord = new LikeRecord();
            likeRecord.setUserId(userId);
            likeRecord.setTargetType("comment");
            likeRecord.setTargetId(commentId);
            likeRecord.setTenantId("default"); // 根据实际情况设置租户ID
            likeRecord.setCreatedAt(LocalDateTime.now());
            likeRecordMapper.insert(likeRecord);
            
            // 增加评论的点赞数
            comment.setLikesCount(comment.getLikesCount() + 1);
            commentMapper.updateById(comment);
            
            return "点赞成功";
        }
    }

    @Override
    public Author getUserProfile(Long targetUserId, Long currentUserId) {
        // 获取目标用户信息
        User targetUser = userService.getById(targetUserId);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 构造Author对象
        Author author = new Author();
        author.setId(targetUser.getId());
        author.setName(targetUser.getUsername());
        author.setAvatar(targetUser.getAvatarUrl());
        author.setHandle(targetUser.getUsername());
        author.setFollowers(targetUser.getFollowers());
        author.setIsVerified(false); // 根据实际业务设置
        
        // 检查当前用户是否关注了目标用户
        boolean isFollowed = followService.isFollowed(currentUserId, targetUserId);
        author.setIsFollowed(isFollowed);
        
        // 设置统计信息
        StatsData stats = new StatsData();
        stats.setWorks(targetUser.getWorks());
        stats.setFollowers(targetUser.getFollowers());
        stats.setLikes(targetUser.getLikes());
        author.setStats(stats);
        
        // 设置其他信息
        author.setComputingPower(targetUser.getComputingPower());
        author.setMaxcomputingPower(1000); // 根据实际情况设置最大算力
        author.setIsMember(userService.isMember(targetUserId));
        author.setBio(""); // 根据实际情况设置用户简介
        
        return author;
    }

    @Override
    public Result<List<PostResponse>> getUserWorks(Long targetUserId, Long currentUserId) {
        // 检查目标用户是否存在
        User targetUser = userService.getById(targetUserId);
        if (targetUser == null) {
            return Result.error(404, "用户不存在", null);
        }
        
        // 查询目标用户的作品列表
        LambdaQueryWrapper<Posts> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Posts::getUserId, targetUserId)
                .eq(Posts::getIsDeleted, false)
                .orderByDesc(Posts::getCreatedAt);
        
        List<Posts> postsList = postsMapper.selectList(queryWrapper);
        
        // 转换为PostResponse列表
        List<PostResponse> postResponses = postsList.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setLikesCount(post.getLikesCount());
            response.setCommentsCount(post.getCommentsCount());
            String tmpImageUrl = tencentCOSUtil.getTmpImageUrl(post.getImageUrl(), 30);
            response.setImageUrl(tmpImageUrl);
            response.setIsDeleted(post.getIsDeleted());
            response.setCreatedAt(post.getCreatedAt());
            response.setUpdatedAt(post.getUpdatedAt());
            
            // 设置作者信息
            Author author = new Author();
            author.setId(targetUser.getId());
            author.setName(targetUser.getUsername());
            author.setAvatar(targetUser.getAvatarUrl());
            author.setHandle(targetUser.getUsername());
            author.setFollowers(targetUser.getFollowers());
            author.setIsVerified(false); // 根据实际业务设置
            
            // 检查当前用户是否关注了目标用户
            boolean isFollowed = followService.isFollowed(currentUserId, targetUserId);
            author.setIsFollowed(isFollowed);
            
            response.setAuthor(author);
            return response;
        }).collect(Collectors.toList());
        
        return Result.success(postResponses);
    }

    @Override
    public List<Author> searchUsers(String keyword, Long currentUserId) {
        // 使用自定义SQL搜索用户并获取关注状态
        List<UserSearchResult> userSearchResults = userMapper.searchUsersByKeyword(keyword, currentUserId);
        
        // 转换为Author对象列表
        return userSearchResults.stream().map(userSearchResult -> {
            Author author = new Author();
            author.setId(userSearchResult.getId());
            author.setName(userSearchResult.getUsername());
            author.setAvatar(userSearchResult.getAvatarUrl());
            author.setHandle(userSearchResult.getUsername());
            author.setFollowers(userSearchResult.getFollowers());
            author.setIsVerified(false); // 根据实际业务设置
            author.setIsFollowed(userSearchResult.getIsFollowed() != null && userSearchResult.getIsFollowed() == 1);
            return author;
        }).collect(Collectors.toList());
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