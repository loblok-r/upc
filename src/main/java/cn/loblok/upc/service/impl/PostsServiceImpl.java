package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.*;
import cn.loblok.upc.entity.Posts;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.PostsMapper;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.PostsService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户作品/帖子表 服务实现类
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
@Service
@Slf4j
@AllArgsConstructor
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {


    private final  FollowService followService;

    private final UserService userService;


    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    @Override
    public List<PostResponse> getRecommendPosts(Long userId) {

        // 简单实现：获取所有帖子按点赞数倒序排列
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("likes_count");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper).stream()
                .map(post -> {
                    Author author = new Author();
                    User user = userService.getById(post.getUserId());
                    author.setId(user.getId());
                    author.setName(user.getUsername());
                    author.setAvatar(user.getAvatarUrl());
                    author.setHandle(user.getUsername());
                    author.setFollowers(user.getFollowers());
                    PostResponse response = new PostResponse();
                    response.setTitle(post.getTitle());
                    response.setContent(post.getContent());
                    response.setAuthor(author);
                    response.setCommentsCount(post.getCommentsCount());
                    response.setImageUrl(post.getImageUrl());
                    response.setLikesCount(post.getLikesCount());
                    response.setCreatedAt(post.getCreatedAt());
                    response.setUpdatedAt(post.getUpdatedAt());
                    return response;
                })
                .toList();
    }

    @Override
    public List<PostResponse> getFollowingPosts(Long userId) {
        // 先获取当前用户关注的所有用户ID
        List<Long> followingIds = followService.getFollowingList(userId).stream()
                .map(user -> user.getId())
                .toList();

        if (followingIds.isEmpty()) {
            return List.of(); // 如果没有关注任何人，则返回空列表
        }

        // 查询关注用户发布的帖子
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", followingIds);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper).stream()
                .map(post -> {
                    Author author = new Author();
                    User user = userService.getById(post.getUserId());
                    author.setId(user.getId());
                    author.setName(user.getUsername());
                    author.setAvatar(user.getAvatarUrl());
                    author.setHandle(user.getUsername());
                    author.setFollowers(user.getFollowers());
                    PostResponse response = new PostResponse();
                    response.setTitle(post.getTitle());
                    response.setContent(post.getContent());
                    response.setAuthor(author);
                    response.setCommentsCount(post.getCommentsCount());
                    response.setImageUrl(post.getImageUrl());
                    response.setLikesCount(post.getLikesCount());
                    response.setCreatedAt(post.getCreatedAt());
                    response.setUpdatedAt(post.getUpdatedAt());
                    return response;
                })
                .toList();
    }

    @Override
    public List<PostResponse> getLatestPosts(Long userId) {
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 20"); // 限制返回20条
        return this.list(queryWrapper).stream()
                .map(post -> {
                    Author author = new Author();
                    User user = userService.getById(post.getUserId());
                    author.setId(user.getId());
                    author.setName(user.getUsername());
                    author.setAvatar(user.getAvatarUrl());
                    author.setHandle(user.getUsername());
                    author.setFollowers(user.getFollowers());
                    PostResponse response = new PostResponse();
                    response.setTitle(post.getTitle());
                    response.setContent(post.getContent());
                    response.setAuthor(author);
                    response.setCommentsCount(post.getCommentsCount());
                    response.setImageUrl(post.getImageUrl());
                    response.setLikesCount(post.getLikesCount());
                    response.setCreatedAt(post.getCreatedAt());
                    response.setUpdatedAt(post.getUpdatedAt());
                    return response;
                })
                .toList();
    }

@Override
public List<PostResponse> getMyPosts(Long userId) {
    // 查询当前用户发布的所有帖子
    QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_id", userId);
    queryWrapper.eq("is_deleted", false);
    queryWrapper.orderByDesc("created_at");

    return this.list(queryWrapper).stream()
            .map(post -> {
                Author author = new Author();
                User user = userService.getById(post.getUserId());
                author.setId(user.getId());
                author.setName(user.getUsername());
                author.setAvatar(user.getAvatarUrl());
                author.setHandle(user.getUsername());
                author.setFollowers(user.getFollowers());

                PostResponse response = new PostResponse();
                response.setId(post.getId());
                response.setTitle(post.getTitle());
                response.setContent(post.getContent());
                response.setAuthor(author);
                response.setCommentsCount(post.getCommentsCount());
                response.setImageUrl(post.getImageUrl());
                response.setLikesCount(post.getLikesCount());
                response.setCreatedAt(post.getCreatedAt());
                response.setUpdatedAt(post.getUpdatedAt());
                return response;
            })
            .toList();
}

    @Override
    public Result<Posts> createPost(Long userId, CreatePostRequest createPostRequest) {
        Posts posts = new Posts();
        // 设置帖子的基本信息
        posts.setUserId(userId);
        posts.setTitle(createPostRequest.getTitle());
        posts.setContent(createPostRequest.getContent());
        posts.setImageUrl(createPostRequest.getImageUrl());
        posts.setCreatedAt(LocalDateTime.now());
        posts.setUpdatedAt(LocalDateTime.now());
        posts.setLikesCount(0);
        posts.setCommentsCount(0);
        posts.setIsDeleted(false);

        // 保存帖子
        boolean saved = this.save(posts);

        //更新用户 user：works
        userService.update(
                null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .apply("works = works + 1")
        );


        if (saved) {
            return  Result.success(posts);
        } else {
            return  Result.error(500, "发布失败", "帖子保存失败");
        }
    }

    @Override
    public List<TComment> getPostComments(Long postId, Long userId) {
        return null;
    }

    @Override
    public void likePost(Long postId, Boolean isLikedBool, Long userId) {

    }


}