package cn.loblok.upc;

import cn.loblok.upc.checkin.CheckinRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class Atest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final ZoneId BUSINESS_TIMEZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private CheckinRecordServiceImpl checkinRecordService;

    @Test
    void testCacheBreakdown() throws InterruptedException {
        // 清理缓存（确保每次测试从空缓存开始）
        redisTemplate.delete("checkin:status:t1:10086:" + LocalDate.now(BUSINESS_TIMEZONE));

        // 重置计数器
        CheckinRecordServiceImpl.dbQueryCount.set(0);

        int threadCount = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    checkinRecordService.hasCheckedInToday("tenant_a", 60L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 等所有线程结束
        executor.shutdown();

        int actualDbQueries = CheckinRecordServiceImpl.dbQueryCount.get();
        System.out.println("【缓存击穿测试】DB 被查询了 " + actualDbQueries + " 次");

        // 预期：无锁版本 → 接近 1000 次；有锁版本 → 1 次
    }
}