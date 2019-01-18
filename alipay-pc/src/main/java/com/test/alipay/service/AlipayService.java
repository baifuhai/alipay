package com.test.alipay.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.google.gson.Gson;
import com.test.alipay.util.AlipayUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class AlipayService {

    private AlipayClient alipayClient;

    public AlipayService() {
        alipayClient = AlipayUtil.getAlipayClient();
    }

    /**
     * 支付
     *
     * @param outTradeNo 商户订单号，必填
     * @param totalAmount 付款金额，必填
     * @param subject 订单名称，必填
     * @param body 商品描述，可选
     * @return
     * @throws Exception
     */
    public AlipayTradePagePayResponse pagePay(String outTradeNo, String totalAmount, String subject, String body) throws Exception {
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayUtil.RETURN_URL);
        alipayRequest.setNotifyUrl(AlipayUtil.NOTIFY_URL);

        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", outTradeNo);
        params.put("product_code", "FAST_INSTANT_TRADE_PAY");
        params.put("total_amount", totalAmount);
        params.put("subject", subject);
        params.put("body", body);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        AlipayTradePagePayResponse resp = alipayClient.pageExecute(alipayRequest);

        return resp;
    }

    /**
     * 交易查询
     *
     * @param tradeNo 支付宝交易号
     * @param outTradeNo 商户订单号，请二选一设置
     * @return
     * @throws Exception
     */
    public AlipayTradeQueryResponse pagePayQuery(String tradeNo, String outTradeNo) throws Exception {
        //设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", tradeNo);
        params.put("out_trade_no", outTradeNo);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        AlipayTradeQueryResponse resp = alipayClient.execute(alipayRequest);

        return resp;
    }

    /**
     * 退款
     *
     * @param tradeNo 支付宝交易号
     * @param outTradeNo 商户订单号，请二选一设置
     * @param refundAmount 需要退款的金额，该金额不能大于订单金额，必填
     * @param refundReason 退款的原因说明
     * @param refundNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws Exception
     */
    public AlipayTradeRefundResponse refund(String tradeNo, String outTradeNo, String refundAmount, String refundReason, String refundNo) throws Exception {
        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", tradeNo);
        params.put("out_trade_no", outTradeNo);
        params.put("refund_amount", refundAmount);
        params.put("refund_reason", refundReason);
        params.put("out_request_no", refundNo);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        AlipayTradeRefundResponse resp = alipayClient.execute(alipayRequest);

        return resp;
    }

    /**
     * 退款查询
     *
     * @param tradeNo 支付宝交易号
     * @param outTradeNo 商户订单号，请二选一设置
     * @param refundNo 请求退款接口时，传入的退款请求号，如果在退款请求时未传入，则该值为创建交易时的外部交易号，必填
     * @return
     * @throws Exception
     */
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String tradeNo, String outTradeNo, String refundNo) throws Exception {
        //设置请求参数
        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", tradeNo);
        params.put("out_trade_no", outTradeNo);
        params.put("out_request_no", refundNo);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        AlipayTradeFastpayRefundQueryResponse resp = alipayClient.execute(alipayRequest);

        return resp;
    }

    /**
     * 关闭交易
     *
     * @param tradeNo 支付宝交易号
     * @param outTradeNo 商户订单号，请二选一设置
     * @return
     * @throws Exception
     */
    public AlipayTradeCloseResponse close(String tradeNo, String outTradeNo) throws Exception {
        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", tradeNo);
        params.put("out_trade_no", outTradeNo);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        AlipayTradeCloseResponse resp = alipayClient.execute(alipayRequest);

        return resp;
    }

    /**
     * 对账单查询
     *
     * @param billType
     * @param billDate
     * @return
     * @throws Exception
     */
    public AlipayDataDataserviceBillDownloadurlQueryResponse billQuery(String billType, String billDate) throws Exception {
        AlipayDataDataserviceBillDownloadurlQueryRequest alipayRequest = new AlipayDataDataserviceBillDownloadurlQueryRequest();

        Map<String, Object> params = new HashMap<>();
        params.put("bill_type", billType);
        params.put("bill_date", billDate);

        alipayRequest.setBizContent(new Gson().toJson(params));

        AlipayDataDataserviceBillDownloadurlQueryResponse resp = alipayClient.execute(alipayRequest);

        return resp;
    }

    /**
     * 验证签名
     *
     * 实际验证过程建议商户务必添加以下校验：
     * 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
     * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
     * 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
     * 4、验证app_id是否为该商户本身。
     *
     * @param request
     * @return
     * @throws Exception
     */
    public boolean signCheck(HttpServletRequest request) throws Exception {
        //获取支付宝GET过来的信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String[] values = (String[]) requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr += (i == 0) ? values[i] : "," + values[i];
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(key, valueStr);
        }

        //验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayUtil.ALIPAY_PUBLIC_KEY, AlipayUtil.CHARSET, AlipayUtil.SIGN_TYPE);
        return signVerified;
    }

}
