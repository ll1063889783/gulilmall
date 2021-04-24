package com.cn.gulimall.oms.vo;

import com.cn.gulimall.oms.entity.OrderEntity;
import lombok.Data;

/**
 * 提交下单响应vo
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//错误状态码 0成功

}
