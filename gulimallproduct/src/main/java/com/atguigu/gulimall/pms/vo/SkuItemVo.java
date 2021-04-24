package com.atguigu.gulimall.pms.vo;

import com.atguigu.gulimall.pms.entity.SkuImagesEntity;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品详情展示
 */
@Data
public class SkuItemVo {

    //1,sku的基本信息 pms_sku_info
    private SkuInfoEntity skuInfo;
    //2,sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;
    //3,获取spu的销售属性的集合
    private List<ItemSaleAttrsVo> saleAttrs;
    //4，获取spu的介绍
    private SpuInfoDescEntity desp;
    //5,获取spu的分组规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;
    //商品是否有货
    private boolean hasStock = true;
    //当前商品的秒杀优惠信息
    private SeckillInfoVo seckillInfoVo;
}
