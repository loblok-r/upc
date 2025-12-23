package cn.loblok.upc.auth.mapper;

import cn.loblok.upc.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
     * 原子增加积分
     * @return 受影响行数（0=用户不存在）
     */
    @Update("UPDATE user SET points = points + #{delta} WHERE id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 原子减少积分（需保证 points >= delta）
     * @return 受影响行数（0=用户不存在 或 积分不足）
     */
    @Update("UPDATE user SET points = points - #{delta} WHERE id = #{userId} AND points >= #{delta}")
    int reducePoints(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 原子性增加算力
     * @param userId 用户ID
     * @param amount 要增加的算力值（应为正数）
     * @return 受影响的行数（1 表示成功，0 表示失败）
     */
    @Update("UPDATE user SET computing_power = computing_power + #{amount} " +
            "WHERE id = #{userId}")
    int addComputingPower(@Param("userId") Long userId, @Param("amount") Integer amount);

    /**
     * 原子性扣减算力（仅当算力足够时）
     * @param userId 用户ID
     * @param amount 要扣除的算力值（应为正数）
     * @return 受影响的行数（1 表示成功，0 表示失败/算力不足）
     */
    @Update("UPDATE user SET computing_power = computing_power - #{amount} " +
            "WHERE id = #{userId} AND computing_power >= #{amount}")
    int deductComputingPower(@Param("userId") Long userId, @Param("amount") Integer amount);

    /**
     * 根据关键字搜索用户，并判断当前用户是否已关注
     * @param keyword 搜索关键字
     * @param currentUserId 当前用户ID
     * @return 匹配的用户列表
     */
//    @Select({
//        "<script>",
//        "SELECT ",
//        "    u.id,",
//        "    u.username AS username,",
//        "    u.avatar_url AS avatarUrl,",
//        "    u.followers AS followers,",
//        "    CASE ",
//        "        WHEN f.id IS NOT NULL THEN 1 ",
//        "        ELSE 0 ",
//        "    END AS isFollowed",
//        "FROM `user` u",
//        "LEFT JOIN `follow` f ON u.id = f.followee_id AND f.follower_id = #{currentUserId}",
//        "WHERE ",
//        "    (u.username LIKE CONCAT('%', #{keyword}, '%'))",
//        "    AND u.status = 0",  // 注意：原SQL中是status=1，但User实体中0表示正常
//        "ORDER BY ",
//        "    LENGTH(u.username) ASC, ",
//        "    u.followers DESC",
//        "LIMIT 20",
//        "</script>"
//    })
//    List<UserSearchResult> searchUsersByKeyword(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);
}