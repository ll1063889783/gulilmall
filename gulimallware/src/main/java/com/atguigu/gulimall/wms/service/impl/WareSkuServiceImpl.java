package com.atguigu.gulimall.wms.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.wms.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.wms.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.wms.exception.NoStockException;
import com.atguigu.gulimall.wms.feign.OrderFeignService;
import com.atguigu.gulimall.wms.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.wms.service.WareOrderTaskService;
import com.atguigu.gulimall.wms.vo.OrderItemVo;
import com.atguigu.gulimall.wms.vo.OrderVo;
import com.atguigu.gulimall.wms.vo.SkuHasStockVo;
import com.atguigu.gulimall.wms.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.wms.dao.WareSkuDao;
import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @EnableRabbit 开启功能
 * 监听消息：使用@RabbitListener：标注在类或者方法上（监听那些队列即可）
 * @RabbitHandler：标注在方法上(重载区分不同的消息)
 */
//监听库存解锁队列
@RabbitListener(queues = "stock.release.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskServicel;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;


    private void unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        //库存解锁
        this.baseMapper.unLockStock(skuId, wareId, num, detailId);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(detailId);
        //将库存工作单的状态变为已解锁。
        entity.setLockStatus(2);
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();

            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * 默认只要是运行时异常都会回滚
     * 库存解锁的场景
     * 1，下订单成功，订单过期没有支付被系统自动取消或者被用户手动取消都要解锁库存
     * 2，下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。
     * 之前锁定的库存就要自动解锁。
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) throws InvocationTargetException, IllegalAccessException {
        /**
         * 保存库存工作单 wms_ware_order_task
         * 方便追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderNo());
        wareOrderTaskServicel.save(taskEntity);
        //1,按照下单的收货地址，找到一个就近仓库，锁定库存。
        //找到每一个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = this.getBaseMapper().listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());
        Boolean allLock = true;
        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            //如果每一个商品都锁定成功，将当前商品锁定了几件工作单记录发给MQ
            //如果锁定失败，前面保存的工作单信息就回滚。发送出去的消息，即使要解锁记录，由于数据查不到id，所有就不用解锁。
            for (Long wareld : wareIds) {
                //成功就返回1，失败是0
                Long count = this.baseMapper.lockSkuStock(skuId, wareld, hasStock.getNum());
                if (count == 1L) {
                    skuStocked = true;
                    //Todo 告诉MQ库存锁定成功,保存至wms_ware_order_task_detail表
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareld, 1);
                    orderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    //只发id不行，防止回滚以后找不到数据
                    stockLockedTo.setDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
            }
            if (skuStocked == false) {
                //当前商品所有仓库都没锁住
                throw new NoStockException(skuId);
            }
        }
        //代码能走到这，说明商品肯定全都锁定成功了
        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        System.out.println("收到解锁库存的消息");
        StockDetailTo detail = to.getDetail();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        //解锁
        //查询数据库关于这个订单的锁定库存信息
        //有：证明库存锁定成功了.解锁需要看订单情况
        //1、没有这个订单，必须解锁
        //2，有这个订单，不是解锁库存。
        // 订单状态：已取消：解锁库存
        // 没取消： 不能解锁。
        //没有:库存锁定失败，库存回滚了。无需解锁。
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //解锁
            Long id = to.getId();
            //根据库存工作单的id查询库存工作单的信息
            WareOrderTaskEntity taskEntity = wareOrderTaskServicel.getById(id);
            //根据订单号查询订单的状态
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = (OrderVo) r.getData(new TypeReference<OrderVo>());
                //订单不存在
                //订单已经取消了，才能解锁库存
                if (data == null || data.getStatus() == 4) {
                    //当前库存工作单详情，状态为1,为已锁定但是未解锁。
                    if (byId.getLockStatus() == 1) {
                        unLockStock(skuId, detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                    //解锁了库存之后手动回复消息
                    //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                }
            } else {
                //远程调用查询订单状态失败，消息拒绝之后重新放入队列让别人继续消费
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            }
        } else {
            //无需解锁(手动回复消息)
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }

    /**
     * 防止订单服务卡顿，导致过期时间之后关闭订单的时候无法将订单的状态修改为取消状态（状态依旧为新建状态）
     * 库存消息优先到期，查询订单状态为新建状态，就不解锁库存。
     * 导致卡顿的订单，永远不能解锁库存。
     *
     * @param to
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        //查一下最新库存的状态，防止重复解锁(根据订单号查询库存工作单)
        WareOrderTaskEntity entity = wareOrderTaskServicel.getOrderTaskByOrderSn(orderSn);
        Long id = entity.getId();
        //按照库存工作单的id找到所有没有解锁的库存，进行解锁(库存工作单详情表里面的task_id，lock_status状态为新建状态下的)
        List<WareOrderTaskDetailEntity> detailEntityList = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1)
        );
        for (WareOrderTaskDetailEntity detailEntity : detailEntityList) {
            unLockStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum(),detailEntity.getId());
        }


    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;//锁定库存数量
        private List<Long> wareId;
    }

}