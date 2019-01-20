package com.test.wxpay.config;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.InputStream;

public class MyWxpayConfig implements WXPayConfig {

    @Override
    public String getAppID() {
        return null;
    }

    @Override
    public String getMchID() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 1000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 1000;
    }

}
