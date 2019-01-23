package com.test.unionpay.config;

import com.unionpay.acp.sdk.AcpService;
import com.unionpay.acp.sdk.SDKConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyConfig {

    @ConfigurationProperties(prefix = "acpsdk")
    @Bean
    public SDKConfig sdkConfig() {
        SDKConfig config = new SDKConfig();
        return config;
    }

    public static void main(String[] args) {
        //SDKConfig config = SDKConfig.config;

        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", "1.0.0");            //版本号
        data.put("reqType", "utf-8");          //字符集编码 可以使用UTF-8,GBK两种方式

        Map<String, String> reqData  = AcpService.sign(data,"d:\\certs\\acp_test_sign.pfx","000000", "utf-8");
//		Map<String,String> rspData = AcpService.post(reqData,"http://localhost:8080/QRCSample/notifyReceiveSample", config.getEncoding());
//
//		System.out.println(rspData);
    }

}
