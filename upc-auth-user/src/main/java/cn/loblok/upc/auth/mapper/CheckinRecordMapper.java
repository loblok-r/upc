package cn.loblok.upc.auth.mapper;

import cn.loblok.upc.auth.entity.chickin.CheckinRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    @Select({
            "<script>",
            "SELECT checkin_date",
            "FROM checkin_record",
            "WHERE user_id = #{userId}",
            "  AND checkin_date BETWEEN #{startDate} AND #{endDate}",
            "ORDER BY checkin_date ASC",
            "</script>"
    })
    List<LocalDate> selectCheckinDatesByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
