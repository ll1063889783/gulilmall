package com.atguigu.gulimall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/**
 * RabbitMQ的配置
 * 注入json消息转换器
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 使用json序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * #开启发送端确认
     * 服务收到消息就回调
        spring.rabbitmq.publisher-confirms=true
        设置确认回调ConfirmCallback
     #  开启发送端消息抵达队列（queue）的确认
        spring.rabbitmq.publisher-returns=true
     #  只要抵达队列，以异步发送优先回调我们这个returnConfirm
        spring.rabbitmq.template.mandatory=true
        消费端确认（保证每个消息被正确消费，此时才可以broker删除这个消息）
        默认是自动确认,只要消息接收到，客户端自动确认，服务端就会移除这个消息。
            收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了，发生了消息丢失。
            手动确认模式，只要我们没有明确告诉MQ，消息被签收。没有Ack，消息就一直是unacked状态
            即使Consumer宕机，消息不会消失，会重新变为Ready。
            下次有新的consumer连接进来再发给他。
      如何签收消息。
     channel.basicAck(deliveryTag,false)签收，业务成功完成就应该签收
     channel.basicNack(deliveryTag,false,true);拒签，业务失败 拒签
     */
    @PostConstruct //对象（MyRabbitConfig）创建完之后来调用该方法
    public void initRabbitTemplate(){
        //在RabbitTemplate里面设置ConfirmCallback
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *只要消息抵达Broker了，ack就等于true
             * @param correlationData 当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause
             * 做好消息确认机制（publisher,consumer[手动ack]）
             * 每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送。
             */
            @Override
            public void confirm(@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) {
                //服务器收到相关信息。
                System.out.println("confirm->correlationData="+correlationData+"->ack="+ack+"->cause="+cause);
            }
        });
        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message 投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replayText 回复的文本类容
             * @param exchange 当时这个消息发给哪个交换机
             * @param routingKey  当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replayText, String exchange, String routingKey) {
                //报错误了，修改数据库当前消息的状态->错误。
            }
        });
    }
}
