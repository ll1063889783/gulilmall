package com.atguigu.gulimall.sms.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.sms.dao.SeckillSessionDao;
import com.atguigu.gulimall.sms.entity.SeckillSessionEntity;
import com.atguigu.gulimall.sms.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.sms.service.SeckillSessionService;
import com.atguigu.gulimall.sms.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        //当前时间年-月-日格式
        LocalDate now = LocalDate.now();

        LocalDate plus2 = now.plusDays(2);
        //最小时间00:00
        LocalTime min = LocalTime.MIN;
        //最大时间23:59:59.999999999
        LocalTime max = LocalTime.MAX;
        //当前时间的年-月-日 00:00
        LocalDateTime startTime = LocalDateTime.of(now, min);
        //格式化后的开始时间
        String formatStartTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //3天之后的时间年-月-日 23:59:59
        LocalDateTime endTime = LocalDateTime.of(plus2, max);
        //格式化后的结束时间
        String formatEndTime = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //查询sms_seckill_session表
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime, endTime));
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map((session) -> {
                Long id = session.getId();
                //秒杀关联的所有商品信息
                List<SeckillSkuRelationEntity> relationEntityList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationList(relationEntityList);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return list;
    }

}