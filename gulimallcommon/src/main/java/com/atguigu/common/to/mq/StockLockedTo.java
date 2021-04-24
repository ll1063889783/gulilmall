package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * 库存锁定传输对象，rabbitMQ发送的消息对象
 */
@Data
public class StockLockedTo {

    private Long id;//库存工作单id

    private StockDetailTo detail;//库存工作单详情
}
