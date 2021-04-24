package com.cn.gulimall.oms.listener;

import com.cn.gulimall.oms.entity.OrderEntity;
import com.cn.gulimall.oms.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 定时关单功能（30分钟未支付）
 */
@Service
//监听死信交换机转发到的队列
@RabbitListener(queues="order.release.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        try{
            //关闭订单
            orderService.closeOrder(orderEntity);
            //手动调用支付宝收单

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(Exception e){
            //关单失败，将消息重新加入消息队列给其他人接收
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        }

    }
}
