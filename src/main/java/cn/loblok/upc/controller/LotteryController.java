package cn.loblok.upc.controller;

import cn.loblok.upc.annotation.CurrentUser;
import cn.loblok.upc.dto.DrawResultDTO;
import cn.loblok.upc.dto.Result;
import cn.loblok.upc.service.LotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 抽奖控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lottery")
public class LotteryController {


    @Autowired
    private LotteryService lotteryService;


    @PostMapping("/draw")
    public Result<DrawResultDTO> drawLottery(@CurrentUser Long userId) {
        log.info("收到抽奖请求");



        return  lotteryService.draw(userId);
    }
}