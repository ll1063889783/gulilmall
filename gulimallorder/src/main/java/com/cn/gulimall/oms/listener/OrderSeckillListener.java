package com.cn.gulimall.oms.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.cn.gulimall.oms.entity.OrderEntity;
import com.cn.gulimall.oms.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "order.seckill.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        try{
            log.info("准备创建秒杀单的详细信息");
            //创建一个秒杀订单
            orderService.createSeckillOrder(seckillOrderTo);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(Exception e){
            //关单失败，将消息重新加入消息队列给其他人接收
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        }

    }

}