package com.atguigu.gulimall.wms.entity;

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
 * @date 2020-08-09 16:57:11
 */
@Data
@TableName("wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long purchaseId;
	/**
	 * 
	 */
	private Long skuId;
	/**
	 * 
	 */
	private Integer skuNum;
	/**
	 * 
	 */
	private BigDecimal skuPrice;
	/**
	 * 
	 */
	private Long wareId;
	/**
	 * ״̬[0
	 */
	private Integer status;

}
