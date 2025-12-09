package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.DrawResultDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.dto.UserDrawInfo;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.entity.User;
import cn.loblok.upc.enums.CommonStatusEnum;
import cn.loblok.upc.service.LotteryService;
import cn.loblok.upc.service.ProductsService;
import cn.loblok.upc.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {


    private final UserService userService;

    private final ProductsService productsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DrawResultDTO> draw(Long userId) {

        User user =  userService.getById(userId);


        if (user == null) {
            return Result.error(CommonStatusEnum.USER_NOT_FOUND.getCode(),CommonStatusEnum.USER_EMPTY_ERROR.getMessage());
        }

        // 检查抽奖次数
        if (user.getLotteryCounts() <= 0) {
            return Result.error("抽奖次数不足");
        }

        // todo 扣减次数，原子操作，生产环境 需要换数据库乐观锁或 Redis 分布式锁

        user.setLotteryCounts(user.getLotteryCounts() - 1);
        userService.updateById(user); // 简化写法，实际建议用 updateWrapper 只更新字段

        // 4. 随机抽取奖品
        Products prize = productsService.drawRandomPrize();

        // 5. 构造返回结果
        DrawResultDTO result = new DrawResultDTO();
        result.setPrizeId(prize.getId());

        UserDrawInfo userInfo = new UserDrawInfo();
        userInfo.setId(user.getId());
        userInfo.setLotteryCounts(user.getLotteryCounts());
        result.setUser(userInfo);

        log.info("用户 {} 抽中奖品: {}, 剩余次数: {}", user.getId(), prize.getId(), user.getLotteryCounts());

        return Result.success(result);
    }
}