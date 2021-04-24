package com.atguigu.gulimall.pms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {
    private String catalog1Id; //1,一级父分类id
    private List<Catelog3Vo> catelog3List;//2，三级子分类
    private String id;
    private String name;
}
