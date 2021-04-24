package com.atguigu.gulimall.sms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:16:47
 */
@Data
@TableName("sms_seckill_sku_relation")
public class SeckillSkuRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long promotionId;
	/**
	 * 
	 */
	private Long promotionSessionId;
	/**
	 * 
	 */
	private Long skuId;
	/**
	 * 
	 */
	private BigDecimal seckillPrice;
	/**
	 * 
	 */
	private BigDecimal seckillCount;
	/**
	 * ÿ
	 */
	private BigDecimal seckillLimit;
	/**
	 * 
	 */
	private Integer seckillSort;

}
