package com.test.wxpay.util;

import com.github.wxpay.sdk.WXPayUtil;
import com.test.wxpay.config.MyWxpayConfig;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * 商户订单号
     *
     * @return
     */
    public static String getOutTradeNo() {
        return sdf.format(new Date()) + new Random().nextInt(1000);
    }

    public static Map<String, String> getAppParamsMap(String prepayId, MyWxpayConfig config) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("appid", config.getAppID());
        params.put("partnerid", config.getMchID());
        params.put("prepayid", prepayId);
        params.put("package", "Sign=WXPay");
        params.put("noncestr", WXPayUtil.generateNonceStr());
        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        String sign = WXPayUtil.generateSignature(params, config.getKey());
        params.put("sign", sign);
        return params;
    }

}
