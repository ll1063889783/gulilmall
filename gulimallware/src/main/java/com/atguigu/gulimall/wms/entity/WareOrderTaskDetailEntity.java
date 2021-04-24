package com.atguigu.gulimall.wms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:57:11
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 
	 */
	private Integer skuNum;
	/**
	 * 
	 */
	private Long taskId;
	/**
	 * 锁定的那个仓库
	 */
	private Long wareId;

	/**
	 * 库存的锁定状态
	 */
	private Integer lockStatus;

}
