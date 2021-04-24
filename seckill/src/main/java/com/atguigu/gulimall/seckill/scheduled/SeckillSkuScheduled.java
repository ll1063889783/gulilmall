package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架：
 * 每天晚上3点：上架最近三天需要秒杀的商品
 * 当天00:00:00 - 23:59:59
 * 明天00:00:00 - 23:59:59
 * 后天00:00:00 - 23:59:59
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    private static final String upload_lock = "seckill:upload:lock";
    //Todo 幂等性处理
    @Async
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Day3(){
        log.info("上架秒杀商品信息。。。");
        //处理幂等性，加分布式锁
        //锁的业务执行完成，状态已经更新完成。释放锁之后，其他人就会获取最新的锁的状态
        RLock lock = redissonClient.getLock(upload_lock);
        //分布式锁锁住10秒
        lock.lock(10, TimeUnit.SECONDS);;
        //重新上架无需处理
        try{
            seckillService.uploadSeckillSkuLatest3Day3();
        }finally{
            //分布式锁解锁
            lock.unlock();
        }

    }

}
