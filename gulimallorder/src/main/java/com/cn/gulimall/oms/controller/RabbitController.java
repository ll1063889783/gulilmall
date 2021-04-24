package com.cn.gulimall.oms.controller;

import com.cn.gulimall.oms.entity.OrderEntity;
import com.cn.gulimall.oms.entity.OrderItemEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping(value="/sendMq")
    public String sendMq(@RequestParam(value="num") Integer num){

            for (int i=0;i<num;i++){
                if(i%2==0){
                    OrderEntity orderEntity = new OrderEntity();
                    orderEntity.setAutoConfirmDay(1);
                    orderEntity.setBillContent("2323");
                    //测试发送消息
                    rabbitTemplate.convertAndSend("java-exchange","java-resr",orderEntity,new CorrelationData(UUID.randomUUID().toString()));
                } else {
                    OrderItemEntity orderItemEntity = new OrderItemEntity();
                    orderItemEntity.setSpuName(UUID.randomUUID().toString());
                    //测试发送消息
                    rabbitTemplate.convertAndSend("java-exchange","java-resr",orderItemEntity,new CorrelationData(UUID.randomUUID().toString()));
                }
            }
            return "ok";
    }
}
