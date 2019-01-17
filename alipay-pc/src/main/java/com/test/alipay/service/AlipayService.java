package com.test.alipay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.google.gson.Gson;
import com.test.alipay.config.AlipayConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayService {

    /**
     * 支付
     *
     * @param outTradeNo 商户订单号，商户网站订单系统中唯一订单号，必填
     * @param totalAmount 付款金额，必填
     * @param subject 订单名称，必填
     * @param body 商品描述，可选
     * @return
     * @throws Exception
     */
    public String pagePay(String outTradeNo, String totalAmount, String subject, String body) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, AlipayConfig.format, AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", outTradeNo);
        params.put("product_code", "FAST_INSTANT_TRADE_PAY");
        params.put("total_amount", totalAmount);
        params.put("subject", subject);
        params.put("body", body);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();

        return result;
    }

    /**
     * 交易查询
     *
     * @param tradeNo 支付宝交易号
     * @param outTradeNo 商户订单号，商户网站订单系统中唯一订单号，请二选一设置
     * @return
     * @throws Exception
     */
    public String pagePayQuery(String tradeNo, String outTradeNo) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, AlipayConfig.format, AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", tradeNo);
        params.put("out_trade_no", outTradeNo);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        return result;
    }

}
