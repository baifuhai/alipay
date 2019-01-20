package com.test.wxpay.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {

    @Bean
    public WXPay wxPay(MyWxpayConfig wxpayConfig) {
        return new WXPay(wxpayConfig, WXPayConstants.SignType.HMACSHA256, true);
    }

    @Bean
    public MyWxpayConfig wxpayConfig() {
        return new MyWxpayConfig();
    }

}
