package com.cn.gulimall.oms.config;

import com.cn.gulimall.oms.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {

    /**
     * 监听死信交换机转发路由后的队列
     * @param orderEntity
     */
    @RabbitListener(queues = "order.release.queue")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单"+orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
    /**
     * 容器中 的Binding，Queue，Exchange都会自动创建（RabbitMQ没有的情况）
     * RabbitMQ只要创建好，@Bean声明属性发生变化也不会覆盖。
     * @return
     */
    //死信队列
    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        //消息过期之后转到死信交换机
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        //死信消息发到交换机上的路由key
        arguments.put("x-dead-letter-routing-key","order.release.order");
        //消息的过期时间
        arguments.put("x-message-ttl",1800000);
        Queue queue = new Queue("order.delay.queue", true, false, false,arguments);
        return queue;
    }
    //死信交换机通过路由key转达的队列
    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue("order.release.queue", true, false, false);
        return queue;
    }
    //死信交换机
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }
    //绑定死信交换机和死信队列
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }
    //绑定死信交换机和转发队列
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseStockBinding(){
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,"stock-event-exchange","order.release.other.#",null);
    }

    @Bean
    public Queue orderSeckillOrderQueue(){
        return new Queue("order.seckill.queue",true,false,false);
    }

    @Bean
    public Binding orderSeckillOrderQueueBinding(){
        return new Binding("order.seckill.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.seckill.order",null);
    }


}
