package com.cn.gulimall.oms;

import com.cn.gulimall.oms.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GulimallOrderApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class,args);
    }

    @Test
    public void sendMessageTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setAutoConfirmDay(1);
        orderEntity.setBillContent("2323");
        //测试发送消息
        rabbitTemplate.convertAndSend("java-exchange","java-resr",orderEntity);
    }

    /**
     * 1、如何创建Exchange、Queue、Binding
     * 使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Test
    public void createExchange(){
        DirectExchange directExchange = new DirectExchange("java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
    }
    @Test
    public void createQueue(){
        Queue queue = new Queue("java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
    }

    @Test
    public void createBinding(){
        //String destination,目的地，绑定队列
        //Binding.DestinationType destinationType,目的地类型
        // String exchange,交换机
        // String routingKey,路由键
        // Map<String, Object> arguments自定义参数
        //将exchange指定的交换机和destination目的地进行绑定
        //使用routingKey作为指定的路由键
        Binding binding = new Binding(
                "java-queue",
                Binding.DestinationType.QUEUE,
                "java-exchange",
                "java-#",
                null
              );
        amqpAdmin.declareBinding(binding);
    }
}
