package com.atguigu.gulimall.pms.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * Ʒ
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:25
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Ʒ
	 */
	@TableId
	@NotNull(message = "修改必须指定品牌id",groups = (UpdateGroup.class))
	@Null(message = "新增不能指定id",groups = (AddGroup.class))
	private Long brandId;
	/**
	 * Ʒ
	 */
	@NotBlank(message = "品牌名不能为空",groups = {UpdateGroup.class,AddGroup.class})
	private String name;
	/**
	 * Ʒ
	 */
	@URL(message = "logo必须是一个合法的url")
	private String logo;
	/**
	 * 
	 */
	private String descript;
	/**
	 * 
	 */
	@ListValue(vals={0,1})
	private Integer showStatus;
	/**
	 * 
	 */
	@Pattern(regexp = "/^[a-zA-Z]$/", message="首字母必须为一个字母")
	private String firstLetter;
	/**
	 * 
	 */
	@NotNull
	@Min(value = 0,message = "排序必须大于0")
	private Integer sort;

}
