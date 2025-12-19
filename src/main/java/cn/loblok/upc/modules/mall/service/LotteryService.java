package cn.loblok.upc.modules.mall.service;

import cn.loblok.upc.modules.mall.dto.DrawResultDTO;
import cn.loblok.upc.common.base.Result;

/**
 * 抽奖服务接口
 */
public interface LotteryService {

    /**
     * 抽奖
     * @return 抽奖结果
     */
     Result<DrawResultDTO> draw(Long userId);


}