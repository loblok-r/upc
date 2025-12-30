package cn.loblok.upc.community.mapper;


import cn.loblok.upc.community.entity.Posts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户作品/帖子表 Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-11
 */
public interface PostsMapper extends BaseMapper<Posts> {

    /**
     * 获取半年内最热(点赞量）的2000条帖子，按点赞量,时间排序
     *
     * @param counts 
     * @return 推荐帖子列表
     */
    @Select("SELECT id,likes_count,created_at FROM posts WHERE is_deleted = 0 and created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "ORDER BY likes_count DESC, created_at DESC LIMIT #{counts}")
    List<Posts> getHotAndLatestPosts(int counts);
}
