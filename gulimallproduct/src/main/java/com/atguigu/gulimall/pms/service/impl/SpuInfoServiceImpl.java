package com.atguigu.gulimall.pms.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.pms.entity.*;
import com.atguigu.gulimall.pms.feign.SearchFeignService;
import com.atguigu.gulimall.pms.feign.WareFeignClient;
import com.atguigu.gulimall.pms.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.pms.dao.SpuInfoDao;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private WareFeignClient wareFeignClient;

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        //1,查出当前spuid对应的所有sku信息、品牌的名字。
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //Todo 4,查询当前sku的所有可以被用来检索的规格属性
        //查pms_product_attr_value，根据spuId查询所有的属性相关信息
        //List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrListForSpu(spuId);
       /* List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAtrId = attrService.selectSearchAttrs(attrIds);

        Set<Long> idSet = new HashSet<>(searchAtrId);
        List<SkuEsModel.Attrs> baseAttrs = baseAttrs.stream().filter(item->{
            return idSet.contains(item.getAttrId());
        }).map(item ->{
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item,attrs1);
            return attrs1;
        }).collect(Collectors.toList());*/
        Map<Long, Boolean> map = null;
        try{
            //2，发送远程调用，库存系统查询是否有库存
            R<List<SkuHasStockVo>> skusHasStock = wareFeignClient.getSkusHasStock(skuIdList);
            map = skusHasStock.getData().stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, sku -> sku.getHasStock()));
        }catch (Exception e){
                log.error("库存服务查询异常，原因{}",e);
        }
        /**
         * private List<Attrs> attrs;
         */
        //设置库存信息
        //2,封装每个sku的信息
        Map<Long, Boolean> finalMap = map;
        List<SkuEsModel> skuEsModelList = skus.stream().map(sku -> {
            //3,组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 1，发送远程调用，库存系统查询是否有库存
           if(finalMap == null){
                skuEsModel.setHasStock(true);
           } else {
               skuEsModel.setHasStock(finalMap.get(sku.getSkuId()));
           }
            skuEsModel.setHasStock(false);
            //ToDo 2,热度评分。0
            skuEsModel.setHotScore(0L);
            //Todo 查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            //查询当前sku的所有可以被用来检索的规格属性
            //设置检索属性
            //skuEsModel.setAttrs(baseAttrs);
            return skuEsModel;
        }).collect(Collectors.toList());
        //Todo 5， 将数据发送给es进行保存 gulimall-search
        //判断远程调用是否成功
        R r = searchFeignService.productStatusUp(skuEsModelList);
        if(r.getCode() == 0){
            //远程调用成功
            //Todo 6、修改当前spu的状态 修改表pms_spu_info的上架状态为已上架(publish_status)
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败，
            //Todo 重复调用，接口幂等性
        }



    }

}