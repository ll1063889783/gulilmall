package com.atguigu.gulimall.wms.listener;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.wms.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.wms.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;
import com.atguigu.gulimall.wms.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


/**
 * @EnableRabbit 开启功能
 * 监听消息：使用@RabbitListener：标注在类或者方法上（监听那些队列即可）
 * @RabbitHandler：标注在方法上(重载区分不同的消息)
 */
//监听库存解锁队列
@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;
    /**
     * 库存自动解锁
     * 1，下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     *     之前锁定的库存就要自动解锁。
     * 2，订单失败。
     *    锁库存失败
     * 只要解锁库存的消息失败，一定要告诉是服务解锁失败
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息。。。。");
        try{
            wareSkuService.unlockStock(to);
            //方法执行正常手动给队列回复消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //方法执行出异常之后，重新放入队列让别人继续消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 订单关闭之后也可以解锁库存
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException{
            System.out.println("订单关闭的消息，准备解锁库存");
        try{
            wareSkuService.unlockStock(to);
            //方法执行正常手动给队列回复消息（第二个参数是否一次性多次回复收到消息）
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //方法执行出异常之后，重新放入队列让别人继续消费（第二个参数是否将消息再次入队列）
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
