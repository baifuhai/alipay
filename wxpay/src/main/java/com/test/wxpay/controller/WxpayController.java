package com.test.wxpay.controller;

import com.github.wxpay.sdk.WXPay;
import com.google.gson.Gson;
import com.test.wxpay.config.MyWxpayConfig;
import com.test.wxpay.util.MyUtil;
import com.test.wxpay.util.ResponseBean;
import com.test.wxpay.util.WebUtil;
import com.wxpay.api.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("wxpay")
@Controller
public class WxpayController {

    @Autowired
    MyWxpayConfig wxpayConfig;

    @Autowired
    WXPay wxpay;

    final String success = "SUCCESS";
    final String failure = "FAIL";

    /**
     * 统一下单
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("pay")
    public void pay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String body = request.getParameter("body");
            String totalFee = request.getParameter("totalFee");
            String tradeType = request.getParameter("tradeType");

            Map<String, String> params = new HashMap<>();
            params.put("body", body);//商品描述
            params.put("out_trade_no", MyUtil.getOutTradeNo());//商户订单号
            params.put("total_fee", totalFee);//总金额
            params.put("spbill_create_ip", request.getRemoteAddr());//终端IP
            params.put("notify_url", "http://localhost:8080/wxpay/notify");//通知地址
            params.put("trade_type", tradeType);//交易类型

            Map<String, String> resp = wxpay.unifiedOrder(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                WebUtil.writeJson(new Gson().toJson(ResponseBean.getFailure(failure, resp)), response);
                return;
            }

            String tradeType2 = resp.get("trade_type");
            String prepayId = resp.get("prepay_id");
            String codeUrl = resp.get("code_url");
            if ("APP".equals(tradeType2) || "JSAPI".equals(tradeType2)) {
                Map<String, String> map = MyUtil.getAppParamsMap(prepayId, wxpayConfig);
                WebUtil.writeJson(new Gson().toJson(ResponseBean.getSuccess(map)), response);
            } else if ("NATIVE".equals(tradeType2)) {
                WebUtil.urlToImage(codeUrl, response.getOutputStream());
            } else {
                WebUtil.writeJson(new Gson().toJson(ResponseBean.getFailure(failure, resp)), response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            WebUtil.writeJson(new Gson().toJson(ResponseBean.getFailure(failure)), response);
        }
    }

    /**
     * 支付结果通知
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("notify")
    @ResponseBody
    public String notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            InputStream is = request.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[10 * 1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.flush();

            String xml = new String(baos.toByteArray(), "UTF-8");

            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            if (wxpay.isPayResultNotifySignatureValid(map)) {
                return "FAIL";
            }

            String returnCode = map.get("return_code");
            String resultCode = map.get("result_code");
            String appId = map.get("appid");
            String mchId = map.get("mch_id");
            String outTradeNo = map.get("out_trade_no");
            String totalFee = map.get("total_fee");

            if (!success.equals(returnCode) ||
                    !success.equals(resultCode) ||
                    !wxpayConfig.getAppID().equals(appId) ||
                    !wxpayConfig.getMchID().equals(mchId) ||
                    !"商户系统中out_trade_no对应的金额".equals(totalFee)) {
                return "FAIL";
            }

            return "<xml>" +
                    "<return_code><![CDATA[SUCCESS]]></return_code>" +
                    "<return_msg><![CDATA[OK]]></return_msg>" +
                    "</xml>";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL";
        }
    }

    /**
     * 查询订单
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("query")
    @ResponseBody
    public ResponseBean query(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String transactionId = request.getParameter("transactionId");
            String outTradeNo = request.getParameter("outTradeNo");

            Map<String, String> params = new HashMap<>();
            params.put("transaction_id", transactionId);//微信订单号
            params.put("out_trade_no", outTradeNo);//商户订单号

            Map<String, String> resp = wxpay.orderQuery(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            String openid = resp.get("openid");
            String tradeType = resp.get("trade_type");
            String tradeState = resp.get("trade_state");
            String tradeStateDesc = resp.get("trade_state_desc");
            String bankType = resp.get("bank_type");
            String totalFee = resp.get("total_fee");
            String transactionId2 = resp.get("transaction_id");
            String outTradeNo2 = resp.get("out_trade_no");
            String timeEnd = resp.get("time_end");

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

    /**
     * 关闭订单
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("close")
    @ResponseBody
    public ResponseBean close(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String outTradeNo = request.getParameter("outTradeNo");

            Map<String, String> params = new HashMap<>();
            params.put("out_trade_no", outTradeNo);//商户订单号

            Map<String, String> resp = wxpay.closeOrder(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

    /**
     * 申请退款
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("refund")
    @ResponseBody
    public ResponseBean refund(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String transactionId = request.getParameter("transactionId");
            String outTradeNo = request.getParameter("outTradeNo");
            String outRefundNo = request.getParameter("outRefundNo");
            String totalFee = request.getParameter("totalFee");
            String refundFee = request.getParameter("refundFee");
            String notifyUrl = request.getParameter("notifyUrl");
            String refundDesc = request.getParameter("refundDesc");

            Map<String, String> params = new HashMap<>();
            params.put("transaction_id", transactionId);//微信订单号
            params.put("out_trade_no", outTradeNo);//商户订单号
            params.put("out_refund_no", outRefundNo);//商户退款单号
            params.put("total_fee", totalFee);//订单金额
            params.put("refund_fee", refundFee);//退款金额
            params.put("refund_desc", refundDesc);//退款原因
            params.put("notify_url", notifyUrl);//退款结果通知url

            Map<String, String> resp = wxpay.closeOrder(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

}
