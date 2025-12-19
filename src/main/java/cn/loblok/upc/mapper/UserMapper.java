package cn.loblok.upc.mapper;

import cn.loblok.upc.modules.auth.entity.User;
import cn.loblok.upc.modules.community.dto.UserSearchResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-11-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据关键字搜索用户，并判断当前用户是否已关注
     * @param keyword 搜索关键字
     * @param currentUserId 当前用户ID
     * @return 匹配的用户列表
     */
    @Select({
        "<script>",
        "SELECT ",
        "    u.id,",
        "    u.username AS username,",
        "    u.avatar_url AS avatarUrl,",
        "    u.followers AS followers,",
        "    CASE ",
        "        WHEN f.id IS NOT NULL THEN 1 ",
        "        ELSE 0 ",
        "    END AS isFollowed",
        "FROM `user` u",
        "LEFT JOIN `follow` f ON u.id = f.followee_id AND f.follower_id = #{currentUserId}",
        "WHERE ",
        "    (u.username LIKE CONCAT('%', #{keyword}, '%'))",
        "    AND u.status = 0",  // 注意：原SQL中是status=1，但User实体中0表示正常
        "ORDER BY ",
        "    LENGTH(u.username) ASC, ",
        "    u.followers DESC",
        "LIMIT 20",
        "</script>"
    })
    List<UserSearchResult> searchUsersByKeyword(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);
}