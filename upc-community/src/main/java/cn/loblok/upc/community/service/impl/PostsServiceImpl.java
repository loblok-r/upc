package cn.loblok.upc.community.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.loblok.rabbit.constants.MQConstants;
import cn.loblok.upc.api.user.dto.UserPublicInfoDTO;
import cn.loblok.upc.api.user.feign.UserFeignClient;
import cn.loblok.upc.api.worker.dto.StatUpdateMsgDTO;
import cn.loblok.upc.common.base.Result;
import cn.loblok.upc.common.enums.PostsTab;
import cn.loblok.upc.common.utils.KeyUtils;
import cn.loblok.upc.common.utils.RedisUtils;
import cn.loblok.upc.community.dto.*;
import cn.loblok.upc.community.entity.Posts;
import cn.loblok.upc.community.mapper.PostsMapper;
import cn.loblok.upc.community.service.FollowService;
import cn.loblok.upc.community.service.PostsService;
import cn.loblok.upc.common.utils.ImageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
@AllArgsConstructor
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {


    private final FollowService followService;

    private final UserFeignClient userFeignClient;

    private final RabbitTemplate rabbitTemplate;

    private final PostCacheService postCacheService;

    private final StringRedisTemplate redisTemplate;


    /**
     * 获取推荐帖子
     *
     * @param userId 用户ID
     * @return 推荐帖子列表
     */
    @Override
    public List<PostResponse> getPostsTab(Long userId, int page, int pageSize, PostsTab postsTab) {


        log.info("获取帖子列表,用户ID:{},page:{},pageSize:{},postsTab:{}", userId, page, pageSize, postsTab.getDescription());
        List<Long> authorIdList = new ArrayList<>();
        List<Long> followedIds = followService.findFollowedIds(userId);
        Page<Posts> postsPage = new Page<>(page, pageSize);
        List<Long> postIds = new ArrayList<>();

        switch (postsTab) {
            case RECOMMEND -> {
                String key = KeyUtils.buildPostTabRecommendPostKey();
                // 从缓存拿 Top 500 进行内存过滤，避免频繁查库
                List<Long> allIds = RedisUtils.getIdsFromRedis(key, 0, 500);

                final List<Long> finalFollowedIds = followedIds;
                if (!allIds.isEmpty()) {
                    // 批量获取详情进行过滤，漏斗模型
                    List<Posts> allPosts = postCacheService.getPostsBatch(allIds);
                    postIds = allPosts.stream()
                            .filter(p -> !finalFollowedIds.contains(p.getUserId())) // 排除已关注
                            .map(Posts::getId)
                            .skip((long) (page - 1) * pageSize)
                            .limit(pageSize)
                            .toList();
                }
                // 兜底：如果缓存为空或过滤后没数据，走原有的数据库查询
                if (postIds.isEmpty()) {
                    postIds = getPostIdsFromDb(postsTab, page, pageSize, followedIds);
                }
                // 推荐页通常不显示“已关注”标签，所以传空列表给 Response
                followedIds = new ArrayList<>();
            }
            case LATEST -> {
                String key = KeyUtils.buildPostTabLatestPostKey();
                int start = (page - 1) * pageSize;
                int end = start + pageSize - 1;
                postIds = RedisUtils.getIdsFromRedis(key, start, end);

                if (postIds.isEmpty()) {
                    postIds = getPostIdsFromDb(postsTab, page, pageSize, null);
                }
            }
            case FOLLOW -> {
                // 关注页数据量因人而异，暂时维持原有的数据库分页查询
                if (followedIds.isEmpty()) return Collections.emptyList();
                return getFollowTabResponses(followedIds, page, pageSize);
            }

            default -> {
               return getOwnerPosts(userId, page, pageSize);
            }
        }
        // 将最终确定的 postIds 转换为统一的 Page 对象和作者 ID 列表
        postsPage = convertIdsToPage(postIds, page, pageSize, postsTab);
        authorIdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();

        return getPostResponses(authorIdList, postsPage, followedIds);
    }

    /**
     * 从数据库中获取帖子 ID 列表（作为 Redis 缺失时的兜底）
     */
    private List<Long> getPostIdsFromDb(PostsTab postsTab, int page, int pageSize, List<Long> followedIds) {
        log.warn("缓存未命中，触发数据库兜底查询: tab={}, page={}", postsTab, page);

        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        queryWrapper.select("id"); // 关键：只查 ID，减少网络 IO 和内存占用

        if (postsTab == PostsTab.RECOMMEND) {
            // 推荐逻辑：按点赞排序
            if (followedIds != null && !followedIds.isEmpty()) {
                queryWrapper.notIn("user_id", followedIds);
            }
            queryWrapper.orderByDesc("likes_count", "created_at");
        } else {
            // 最新逻辑：按时间排序
            queryWrapper.orderByDesc("created_at");
        }

        // 执行分页查询
        Page<Posts> dbPage = new Page<>(page, pageSize);
        this.page(dbPage, queryWrapper);

        return dbPage.getRecords().stream().map(Posts::getId).toList();
    }

    private List<PostResponse> getFollowTabResponses(List<Long> followedIds, int page, int pageSize) {
        //关注帖子
        if (followedIds.isEmpty()) {
            return List.of(); // 如果没有关注任何人，则返回空列表
        }
        // 查询关注用户发布的帖子
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", followedIds);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");

        Page<Posts> postsPage  = new Page<>(page, pageSize);
        this.page(postsPage, queryWrapper);

        List<Long> authorIdList= postsPage.getRecords().stream().map(Posts::getUserId).toList();
        return getPostResponses(authorIdList, postsPage, followedIds);
    }

    //查询个人的帖子
    List<PostResponse> getOwnerPosts(Long userId, int page, int pageSize){
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_deleted", false);
        queryWrapper.orderByDesc("created_at");

        Page<Posts> postsPage = new Page<>(page, pageSize);
        this.page(postsPage, queryWrapper);
        List<Long> authorIdList = postsPage.getRecords().stream().map(Posts::getUserId).toList();
        //空列表，默认为空
        List<Long> followedIds = new ArrayList<>();
        return getPostResponses(authorIdList, postsPage, followedIds);
    }


    private Page<Posts> convertIdsToPage(List<Long> ids, int page, int pageSize, PostsTab postsTab) {
        // 批量从 Redis 详情缓存获取，缺失的会自动回填 DB
        List<Posts> postsList = postCacheService.getPostsBatch(ids);
        Page<Posts> postsPage = new Page<>(page, pageSize);
        postsPage.setRecords(postsList);

        //若是缓存模式，从 ZSet 长度获取，否则保持 0
        String key = postsTab == PostsTab.LATEST ? KeyUtils.buildPostTabLatestPostKey() : KeyUtils.buildPostTabRecommendPostKey();
        Long total = redisTemplate.opsForZSet().zCard(key);
        postsPage.setTotal(total != null ? total : 0);

        return postsPage;
    }


    private List<PostResponse> getPostResponses(List<Long> IdList, Page<Posts> postsPage, List<Long> followedIds) {

        if(IdList.isEmpty()){
            return Collections.emptyList();
        }
        Result<Map<Long, UserPublicInfoDTO>> useMap = userFeignClient.getUserPublicInfoBatch(IdList);
        if (useMap != null && useMap.getData() != null) {
        return postsPage.getRecords().stream()
                .map(post -> {
                    CommunityUserVO vo = new CommunityUserVO();
                    UserPublicInfoDTO dto = useMap.getData().get(post.getUserId());

                    if (dto != null) {
                        BeanUtils.copyProperties(dto, vo);
                    }
                    vo.setIsFollowed(followedIds != null && followedIds.contains(post.getUserId()));
                    PostResponse response = new PostResponse();
                    BeanUtils.copyProperties(post, response);
                    response.setAuthor(vo);
                    response.setImageUrl(ImageUtil.getOptimizedUrl(post.getImageUrl(), false));
                    return response;
                })
                .collect(Collectors.toList());
         }else{
            return List.of();
        }
    }


    @Override
    public Result<Posts> createPost(Long userId, CreatePostRequest createPostRequest) {
        Posts posts = new Posts();
        // 设置帖子的基本信息
        posts.setUserId(userId);
        BeanUtils.copyProperties(createPostRequest, posts);
        posts.setCreatedAt(LocalDateTime.now());
        posts.setUpdatedAt(LocalDateTime.now());
        posts.setLikesCount(0);
        posts.setCommentsCount(0);
        posts.setIsDeleted(false);

        // 保存帖子
        boolean saved = this.save(posts);

        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);

        StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
        msg.setUserId(userId);
        msg.setDelta(1);
        msg.setPostId(posts.getId());
        msg.setType("POST");
        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_STATS_UPDATE,
                msg,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );

        if (saved) {
            return Result.success(posts);
        } else {
            return Result.error(500, "发布失败", "帖子保存失败");
        }
    }



    @Override
    public void deletePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Posts post = this.getById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查帖子是否属于当前用户
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除他人的帖子");
        }
        String bizId = IdUtil.randomUUID();
        CorrelationData correlationData = new CorrelationData(bizId);

        StatUpdateMsgDTO msg = new StatUpdateMsgDTO();
        msg.setUserId(userId);
        msg.setDelta(-1);
        msg.setType("POST");
        msg.setPostId(postId);
        rabbitTemplate.convertAndSend(
                MQConstants.EXCHANGE_NAME,
                MQConstants.ROUTE_STATS_UPDATE,
                msg,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );



        // 逻辑删除帖子，将is_deleted设置为true
        post.setIsDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        this.updateById(post);
    }
}