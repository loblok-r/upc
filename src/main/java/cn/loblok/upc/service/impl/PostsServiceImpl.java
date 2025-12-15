package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.*;
import cn.loblok.upc.entity.Comment;
import cn.loblok.upc.entity.LikeRecord;
import cn.loblok.upc.entity.Posts;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.mapper.CommentMapper;
import cn.loblok.upc.mapper.LikeRecordMapper;
import cn.loblok.upc.mapper.PostsMapper;
import cn.loblok.upc.service.FollowService;
import cn.loblok.upc.service.PostsService;
import cn.loblok.upc.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {


    private final FollowService followService;

    private final UserService userService;
    
    private final CommentMapper commentMapper;
    
    private final LikeRecordMapper likeRecordMapper;
    
    @Autowired
    public PostsServiceImpl(FollowService followService, UserService userService, 
                           CommentMapper commentMapper, LikeRecordMapper likeRecordMapper) {
        this.followService = followService;
        this.userService = userService;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
    }

    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    @Override
    public List<PostResponse> getRecommendPosts(Long userId, int page, int pageSize) {
        // 简单实现：获取所有帖子按点赞数倒序排列
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("likes_count");
        
        Page<Posts> postsPage = new Page<>(page, pageSize);
        this.page(postsPage, queryWrapper);
        
        return postsPage.getRecords().stream()
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
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getFollowingPosts(Long userId, int page, int pageSize) {
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
        
        Page<Posts> postsPage = new Page<>(page, pageSize);
        this.page(postsPage, queryWrapper);
        
        return postsPage.getRecords().stream()
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
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getLatestPosts(Long userId, int page, int pageSize) {
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");
        
        Page<Posts> postsPage = new Page<>(page, pageSize);
        this.page(postsPage, queryWrapper);
        
        return postsPage.getRecords().stream()
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
                .collect(Collectors.toList());
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
        // 查询帖子的所有评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", "post");
        queryWrapper.eq("target_id", postId);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByAsc("created_at");
        
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        
        // 转换为TComment对象
        return comments.stream().map(comment -> {
            TComment tComment = new TComment();
            tComment.setId(String.valueOf(comment.getId()));
            tComment.setUserId(String.valueOf(comment.getUserId()));
            
            // 获取用户信息
            User user = userService.getById(comment.getUserId());
            tComment.setUser(user);
            
            tComment.setText(comment.getContent());
            tComment.setLikes(comment.getLikesCount());
            
            // 检查当前用户是否点赞了该评论
            QueryWrapper<LikeRecord> likeQueryWrapper = new QueryWrapper<>();
            likeQueryWrapper.eq("user_id", userId);
            likeQueryWrapper.eq("target_type", "comment");
            likeQueryWrapper.eq("target_id", comment.getId());
            tComment.setIsLiked(likeRecordMapper.selectCount(likeQueryWrapper) > 0);
            
            tComment.setCreatedAt(comment.getCreatedAt().toString());
            // 简化处理，实际项目中应该根据创建时间和当前时间计算
            tComment.setTimeAgo("刚刚");
            
            // 这里简化处理，实际项目中可能需要查询回复评论
            tComment.setReplies(new TComment[0]);
            
            return tComment;
        }).toList();
    }

    @Override
    public void likePost(Long postId, Boolean isLiked, Long userId) {
        // 检查帖子是否存在
        Posts post = this.getById(postId);
        if (post == null || post.getIsDeleted()) {
            throw new RuntimeException("帖子不存在或已被删除");
        }
        
        if (isLiked) {
            // 点赞操作
            // 检查是否已经点赞
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("target_type", "post");
            queryWrapper.eq("target_id", postId);
            
            if (likeRecordMapper.selectCount(queryWrapper) <= 0) {
                // 未点赞，执行点赞操作
                LikeRecord likeRecord = new LikeRecord();
                likeRecord.setUserId(userId);
                likeRecord.setTargetType("post");
                likeRecord.setTargetId(postId);
                likeRecord.setTenantId("default"); // 这里应该是从上下文中获取tenantId
                likeRecord.setCreatedAt(LocalDateTime.now());
                likeRecordMapper.insert(likeRecord);
                
                // 更新帖子的点赞数
                post.setLikesCount(post.getLikesCount() + 1);
                this.updateById(post);
            }
        } else {
            // 取消点赞操作
            QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("target_type", "post");
            queryWrapper.eq("target_id", postId);
            
            // 删除点赞记录
            likeRecordMapper.delete(queryWrapper);
            
            // 更新帖子的点赞数
            if (post.getLikesCount() > 0) {
                post.setLikesCount(post.getLikesCount() - 1);
                this.updateById(post);
            }
        }
    }

    @Override
    public Result<TComment> addComment(Long postId, PayloadDTO payloadDTO, Long userId) {
        // 检查帖子是否存在
        Posts post = this.getById(postId);
        if (post == null || post.getIsDeleted()) {
            return Result.error(404, "帖子不存在或已被删除", null);
        }

        // 创建评论对象
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType("post");
        comment.setTargetId(postId);
        comment.setContent(payloadDTO.getContent());
        comment.setLikesCount(0);
        comment.setIsDeleted(false);
        comment.setTenantId("default"); // 在实际应用中应该从上下文中获取
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // 保存评论
        boolean saved = commentMapper.insert(comment) > 0;
        
        if (!saved) {
            return Result.error(500, "评论保存失败", null);
        }

        // 更新帖子的评论数
        post.setCommentsCount(post.getCommentsCount() + 1);
        this.updateById(post);

        // 构造返回的TComment对象
        TComment tComment = new TComment();
        tComment.setId(String.valueOf(comment.getId()));
        tComment.setUserId(String.valueOf(userId));
        
        // 获取用户信息
        User user = userService.getById(userId);
        tComment.setUser(user);
        
        tComment.setText(payloadDTO.getContent());
        tComment.setLikes(0);
        tComment.setIsLiked(false);
        tComment.setCreatedAt(comment.getCreatedAt().toString());
        tComment.setTimeAgo("刚刚");
        tComment.setReplies(new TComment[0]);
        
        return Result.success(tComment);
    }
}