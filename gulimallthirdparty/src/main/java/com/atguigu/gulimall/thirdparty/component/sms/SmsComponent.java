package com.atguigu.gulimall.thirdparty.component.sms;

import com.atguigu.gulimall.thirdparty.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String skin;
    private String sign;
    private String appCode;


    public void sendSmsCode(String phone,String code){
        String method = "Get";
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","APPCODE "+appCode);
        Map<String,String> querys = new HashMap<>();
        querys.put("code",code);//发送的验证码
        querys.put("phone",phone);//验证码送达的手机号
        querys.put("skin",skin);
        querys.put("sign",sign);
        try{
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        }catch (Exception e){

        }
    }
}
