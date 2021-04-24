package com.atguigu.gulimall.wms.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    /**
     * 使用json序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    /**
     * 监听队列默认服务启动之后会创建MyRabbitConfig里面的所有队列、交换机等信息
     *
     */
    @RabbitListener(queues = "stock.delay.queue")
    public void listener(Channel channel, Message message) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    //死信交换机
    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    /**
     * 容器中 的Binding，Queue，Exchange都会自动创建（RabbitMQ没有的情况）
     * RabbitMQ只要创建好，@Bean声明属性发生变化也不会覆盖。
     * @return
     */
    //死信队列
    @Bean
    public Queue stockDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        //消息过期之后转到死信交换机
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        //死信消息发到交换机上的路由key
        arguments.put("x-dead-letter-routing-key","stock.release");
        //消息的过期时间
        arguments.put("x-message-ttl",1800000);
        Queue queue = new Queue("stock.delay.queue", true, false, false,arguments);
        return queue;
    }

    //死信交换机通过路由key转达的队列
    @Bean
    public Queue stockReleaseQueue(){
        Queue queue = new Queue("stock.release.queue", true, false, false);
        return queue;
    }

    //绑定死信交换机和死信队列
    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.release.#",null);
    }
    //绑定死信交换机和转发队列
    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","stock.locked ",null);
    }


}
