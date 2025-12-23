package cn.loblok.upc.auth.mapper;

import cn.loblok.upc.auth.entity.DailyUsage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author loblok
 * @since 2025-12-10
 */
@Mapper
public interface DailyUsageMapper extends BaseMapper<DailyUsage> {

    /**
     * 根据用户ID和日期查询使用记录
     * @param userId
     * @param date
     * @return
     */
    @Select("SELECT * FROM daily_usage WHERE user_id = #{userId} AND date = #{date}")
    DailyUsage selectByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);


    /**
     * 递增使用次数
     * @param userId
     * @param date
     * @param mode
     */
    @Update("UPDATE daily_usage SET " +
            "text_chat_count = CASE WHEN #{mode} = 'TEXT_CHAT' THEN text_chat_count + 1 ELSE text_chat_count END, " +
            "ai_drawing_count = CASE WHEN #{mode} = 'AI_DRAWING' THEN ai_drawing_count + 1 ELSE ai_drawing_count END " +
            "WHERE user_id = #{userId} AND date = #{date}")
    void incrementUsage(@Param("userId") String userId, @Param("date") LocalDate date, @Param("mode") String mode);


}
