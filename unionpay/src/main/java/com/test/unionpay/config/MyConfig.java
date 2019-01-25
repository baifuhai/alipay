package com.test.unionpay.config;

import com.unionpay.acp.sdk.SDKConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {

    @Bean
    public SDKConfig sdkConfig() {
        return SDKConfig.getInstance();
    }

}
