package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        //创建配置
        Config config = new Config();
        //config.useClusterServers().addNodeAddress("127.0.0.1:7004","127.0.0.1:7005");
        config.useSingleServer().setAddress("rediss://127.0.0.1:6379");
        //根据Config创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);

        return redissonClient;
    }

}
