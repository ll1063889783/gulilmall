package com.cn.gulimall.oms.feign;

import com.cn.gulimall.oms.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value="gulimall-member")
public interface MemberFeignService {

    @GetMapping(value="/ums/memberreceiveaddress/{memberId}/address")
    public List<MemberAddressVo> getAddress(@PathVariable(value="memberId") Long memberId);

}
