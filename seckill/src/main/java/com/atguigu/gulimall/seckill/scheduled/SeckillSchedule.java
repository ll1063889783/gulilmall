package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：
 * 1,@EnableScheduling 开启定时任务
 * 2，@Scheduled 开启一个定时任务
 * 自动配置了TaskSchedulingAutoConfiguration类，属性绑定在TaskSchedulingProperties
 *
 * 异步任务：@EnableAsync
 * 给希望异步执行的方法上标注@Async
 * 自动配置了TaskExecutionAutoConfiguration类，属性绑定在TaskExecutionProperties
 * 解决：使用异步+定时任务来完成定时任务不阻塞的功能。
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class SeckillSchedule {
    /**
     * Spring中由6位组成，不允许第7位的年
     * Spring中在周几的位置，1-7代表周一到周日，或者MON-SUN
     * 定时任务不应该阻塞，默认是阻塞的。
     *  1，可以让业务运行以异步的方式，自己提交到线程池
     *    CompletableFuture.runAsync(()->{
     *        xxxService.hello();
     *    },executor);
     *  2,支持定时任务线程池
     *  设置TaskSchedulingProperties
     *  3，让定时任务异步执行
     *  异步任务：@EnableAsync
     *  给希望异步执行的方法上标注@Async
     * 语法：秒 分 时 日 月 周
     */
    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello(){
        log.info("hello....");
    }
}
