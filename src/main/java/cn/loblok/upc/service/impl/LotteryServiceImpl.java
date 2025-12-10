package cn.loblok.upc.service.impl;

import cn.loblok.upc.dto.DrawResultDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.dto.UserDrawInfo;
import cn.loblok.upc.entity.Products;
import cn.loblok.upc.entity.UserItems;
import cn.loblok.upc.enums.UserItemType;
import cn.loblok.upc.service.LotteryService;
import cn.loblok.upc.service.ProductsService;
import cn.loblok.upc.service.UserItemsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {


    private final UserItemsService userItemsService;

    private final ProductsService productsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DrawResultDTO> draw(Long userId) {

        List<UserItems> chances =  userItemsService.getByUserId(userId, UserItemType.LOTTERY_TICKET);

        // 检查抽奖次数
        if (chances.isEmpty()) {
            return Result.error("抽奖次数不足");
        }

        // todo 扣减次数，原子操作，生产环境 需要换数据库乐观锁或 Redis 分布式锁

        for (UserItems chance : chances) {
            // 尝试乐观锁扣减（推荐）
            int updated = userItemsService.consumeOneChanceWithOptimisticLock(chance.getId());
            if (updated > 0) {
                // 扣减成功，退出
               break;
            }
            // 如果失败（quantity 被别人改了），继续下一条
        }
        //获取扣减后的总次数
        int totalLotteryChances = userItemsService.getTotalLotteryChances(userId);

        // 随机抽取奖品
        Products prize = productsService.drawRandomPrize();

        // 构造返回结果
        DrawResultDTO result = new DrawResultDTO();
        result.setPrizeId(prize.getId());

        UserDrawInfo userInfo = new UserDrawInfo();
        userInfo.setId(userId);
        result.setUser(userInfo);

        log.info("用户 {} 抽中奖品: {}, 剩余次数: {}", userId, prize.getId(),totalLotteryChances);

        return Result.success(result);
    }
}