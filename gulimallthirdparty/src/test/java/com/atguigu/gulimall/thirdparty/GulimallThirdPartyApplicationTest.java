package com.atguigu.gulimall.thirdparty;


import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 1,引入oss-starter
 * 2，配置key，endpoint相关信息即可
 * 3，使用OSSClient 进行相关操作
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTest {

    @Autowired
    private OSSClient ossClient;

    @Test
    public void context() throws Exception{
      /*  // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-heyuan.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = "LTAI4GEdwRozMbHr7B5cHUyx";
        String accessKeySecret = "BqeqNfwHvIGvslagcUjJLgy85mrAqC";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
*/
        // 上传文件流。
        InputStream inputStream = new FileInputStream("F:\\qq\\QQ图片20150331090915.jpg");
        ossClient.putObject("gulimall-liliang", "QQ图片20150331090915.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功、、、、");
    }

}
