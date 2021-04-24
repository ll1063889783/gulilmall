package com.atguigu.gulimall.pms.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.pms.dao.SkuInfoDao;
import com.atguigu.gulimall.pms.entity.SkuImagesEntity;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;
import com.atguigu.gulimall.pms.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.pms.feign.SeckillFeignService;
import com.atguigu.gulimall.pms.service.*;
import com.atguigu.gulimall.pms.vo.ItemSaleAttrsVo;
import com.atguigu.gulimall.pms.vo.SeckillInfoVo;
import com.atguigu.gulimall.pms.vo.SkuItemVo;
import com.atguigu.gulimall.pms.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {


    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SkuInfoEntity> list = this.list(queryWrapper);
        return list;
    }

    @Override
    public SkuInfoEntity getBySkuId(Long skuId) {
        //sku的基本信息 pms_sku_info
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        SkuInfoEntity entity = this.getOne(queryWrapper);
        return entity;
    }

    @Override
    public SkuItemVo item(Long skuId) {

        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1,sku的基本信息 pms_sku_info
            SkuInfoEntity skuInfo = getBySkuId(skuId);
            skuItemVo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);

        //获取第一个任务的返回值 result为skuInfo,必须等第一个任务执行完成（并列执行）
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            //3,获取spu的销售属性的集合 pms_sku_sale_attr_value
            List<ItemSaleAttrsVo> saleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrs);
        }, executor);

        //获取第一个任务的返回值 result为skuInfo,必须等第一个任务执行完成（并列执行）
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((result) -> {
            //4，获取spu的介绍 pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(result.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        //获取第一个任务的返回值 result为skuInfo,必须等第一个任务执行完成（并列执行）
        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            //5,获取spu的规格参数信息
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(result.getSpuId(), result.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);
        //当前sku所属于哪个spu
        //Long spuId = skuInfo.getSpuId();
        //当前sku所属于哪个三级分类
        //Long catalogId = skuInfo.getCatalogId();
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2,sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        //查询当前sku商品是否参与秒杀优惠
        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo data = (SeckillInfoVo) r.getData("data", new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfoVo(data);
            }
        }, executor);
        //等到所有任务都完成
        try {
            CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture,secKillFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return skuItemVo;
    }

}