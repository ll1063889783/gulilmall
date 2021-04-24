package com.atguigu.gulimall.pms.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.pms.entity.BrandEntity;
import com.atguigu.gulimall.pms.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.validation.Valid;


/**
 * Ʒ
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:25
 */
@RestController
@RequestMapping("pms/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("pms:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("pms:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    //@RequiresPermissions("pms:brand:info")
    public R info(@RequestParam("brandIds") List<Long> brandId){


        List<BrandEntity> entityList = (List<BrandEntity>)brandService.listByIds(brandId);

        return R.ok().put("brand", entityList);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("pms:brand:save")
    public R save(@Validated({AddGroup.class  }) @RequestBody BrandEntity brand, BindingResult bindingResult){

//        Map<String,Object> map = new HashMap<>();
//        //获取校验的错误结果
//        if(bindingResult.hasErrors()){
//            //List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//            bindingResult.getFieldErrors().forEach((item)->{
//                //获取错误提示
//                String message = item.getDefaultMessage();
//                //获取错误属性的名字
//                String field = item.getField();
//
//                map.put(field,message);
//            });
//            return R.error(400,"数据提交不合法").put("data",map);
//
//        } else {
//
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("pms:brand:update")
    public R update(@RequestBody BrandEntity brand){
		brandService.updateByIdDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("pms:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
