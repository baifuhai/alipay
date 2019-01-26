package com.test.wxpay.controller;

import com.github.wxpay.sdk.WXPay;
import com.google.gson.Gson;
import com.helpwin.bean.ResponseBean;
import com.helpwin.util.WebUtil;
import com.test.wxpay.config.MyWxpayConfig;
import com.test.wxpay.util.AESUtil;
import com.test.wxpay.util.MyUtil;
import com.wxpay.api.WXPayUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            params.put("trade_type", tradeType);//交易类型，JSAPI，NATIVE，APP
            //params.put("product_id", "12");//文档里没有，readme.md里有

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
                //WebUtil.urlToImage(codeUrl, response.getOutputStream());
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
     * 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
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

            /*ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[10 * 1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.flush();

            String xml = new String(baos.toByteArray(), "UTF-8");*/

            String xml = new String(IOUtils.toByteArray(is), "UTF-8");

            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            if (!wxpay.isPayResultNotifySignatureValid(map)) {
                return failure;
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
                return failure;
            }

            return "<xml>" +
                    "<return_code><![CDATA[SUCCESS]]></return_code>" +
                    "<return_msg><![CDATA[OK]]></return_msg>" +
                    "</xml>";
        } catch (Exception e) {
            e.printStackTrace();
            return failure;
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
            String refundDesc = request.getParameter("refundDesc");
            String notifyUrl = request.getParameter("notifyUrl");

            Map<String, String> params = new HashMap<>();
            params.put("transaction_id", transactionId);//微信订单号
            params.put("out_trade_no", outTradeNo);//商户订单号
            params.put("out_refund_no", outRefundNo);//商户退款单号
            params.put("total_fee", totalFee);//订单金额
            params.put("refund_fee", refundFee);//退款金额
            params.put("refund_desc", refundDesc);//退款原因
            params.put("notify_url", notifyUrl);//退款结果通知url

            Map<String, String> resp = wxpay.refund(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            String transactionId2 = resp.get("transaction_id");
            String outTradeNo2 = resp.get("out_trade_no");
            String outRefundNo2 = resp.get("out_refund_no");
            String refundId = resp.get("refund_id");//微信退款单号
            String totalFee2 = resp.get("total_fee");
            String refundFee2 = resp.get("refund_fee");

            String cash_fee = resp.get("cash_fee");//现金支付金额

            String cash_refund_fee = resp.get("cash_refund_fee");//现金退款金额

            String settlement_total_fee = resp.get("settlement_total_fee");//应结订单金额
            String settlement_refund_fee = resp.get("settlement_refund_fee");//应结退款金额

            String coupon_refund_fee = resp.get("coupon_refund_fee");//代金券退款总金额
            String coupon_refund_count = resp.get("coupon_refund_count");//退款代金券使用数量
            String coupon_type_0 = resp.get("coupon_type_0");//代金券类型
            String coupon_type_1 = resp.get("coupon_type_1");//代金券类型
            String coupon_refund_fee_0 = resp.get("coupon_refund_fee_0");//单个代金券退款金额
            String coupon_refund_fee_1 = resp.get("coupon_refund_fee_1");//单个代金券退款金额
            String coupon_refund_id_0 = resp.get("coupon_refund_id_0");//退款代金券ID
            String coupon_refund_id_1 = resp.get("coupon_refund_id_1");//退款代金券ID

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

    /**
     * 查询退款
     *
     * 退款次数超过10次时，可使用微信订单号或商户订单号，和偏移量，最多10笔，默认返回前10笔
     * 退款次数超过20次时，只能使用微信退款单号或商户退款单号单笔查询
     * 每个订单退款次数不能超过50次
     *
     * 100 - 10 - 30 = 60
     * 现金支付60，代金券40
     * 第一次退款20 = 20 * 0.6 + 20 * 0.4 = 12 + 8 = 12 + (8 * 0.25 + 8 * 0.75) = 12 + (2 + 6)
     * 第一次退款80 = 80 * 0.6 + 80 * 0.4 = 48 + 32 = 48 + (32 * 0.25 + 32 * 0.75) = 48 + (8 + 24)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("refundQuery")
    @ResponseBody
    public ResponseBean refundQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String transactionId = request.getParameter("transactionId");
            String outTradeNo = request.getParameter("outTradeNo");
            String outRefundNo = request.getParameter("outRefundNo");
            String refundId = request.getParameter("refundId");
            String offset = request.getParameter("offset");

            Map<String, String> params = new HashMap<>();
            params.put("transaction_id", transactionId);//微信订单号
            params.put("out_trade_no", outTradeNo);//商户订单号
            params.put("out_refund_no", outRefundNo);//商户退款单号
            params.put("refund_id", refundId);//微信退款单号
            params.put("offset", offset);//偏移量，退款次数超过10次时可使用

            Map<String, String> resp = wxpay.refundQuery(params);
            if (!success.equals(resp.get("return_code")) || !success.equals(resp.get("result_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            String transactionId2 = resp.get("transaction_id");
            String outTradeNo2 = resp.get("out_trade_no");

            String total_refund_count = resp.get("total_refund_count");//订单总退款次数，请求参数传入offset后有返回
            String refund_count = resp.get("refund_count");//当前返回退款笔数

            String total_fee = resp.get("total_fee");//订单总金额
            String cash_fee = resp.get("cash_fee");//现金支付金额

            String settlement_total_fee = resp.get("settlement_total_fee");//应结订单金额

            //第1笔退款
            String out_refund_no_0 = resp.get("out_refund_no_0");//商户退款单号
            String refund_id_0 = resp.get("refund_id_0");//微信退款单号
            String refund_channel_0 = resp.get("refund_channel_0");//退款渠道
            String refund_fee_0 = resp.get("refund_fee_0");//退款金额

            //String cash_refund_fee_0 = resp.get("cash_refund_fee_0");//现金退款金额，文档里没有

            String coupon_refund_fee_0 = resp.get("coupon_refund_fee_0");//代金券退款金额
            String coupon_refund_count_0 = resp.get("coupon_refund_count_0");//代金券使用数量
            String coupon_refund_id_0_0 = resp.get("coupon_refund_id_0_0");//代金券ID
            String coupon_refund_id_0_1 = resp.get("coupon_refund_id_0_1");//代金券ID
            String coupon_type_0_0 = resp.get("coupon_type_0_0");//代金券类型
            String coupon_type_0_1 = resp.get("coupon_type_0_1");//代金券类型
            String coupon_refund_fee_0_0 = resp.get("coupon_refund_fee_0_0");//单个代金券退款金额
            String coupon_refund_fee_0_1 = resp.get("coupon_refund_fee_0_1");//单个代金券退款金额
            String refund_status_0 = resp.get("refund_status_0");//退款状态
            String refund_account_0 = resp.get("refund_account_0");//退款资金来源
            String refund_recv_accout_0 = resp.get("refund_recv_accout_0");//退款入账账户
            String refund_success_time_0 = resp.get("refund_success_time_0");//退款成功时间

            //第2笔退款
            String out_refund_no_1 = resp.get("out_refund_no_1");//商户退款单号
            String refund_id_1 = resp.get("refund_id_1");//微信退款单号
            String refund_channel_1 = resp.get("refund_channel_1");//退款渠道
            String refund_fee_1 = resp.get("refund_fee_1");//退款金额

            //String cash_refund_fee_1 = resp.get("cash_refund_fee_1");//现金退款金额，文档里没有

            String coupon_refund_fee_1 = resp.get("coupon_refund_fee_1");//代金券退款金额
            String coupon_refund_count_1 = resp.get("coupon_refund_count_1");//代金券使用数量
            String coupon_refund_id_1_0 = resp.get("coupon_refund_id_1_0");//代金券ID
            String coupon_refund_id_1_1 = resp.get("coupon_refund_id_1_1");//代金券ID
            String coupon_type_1_0 = resp.get("coupon_type_1_0");//代金券类型
            String coupon_type_1_1 = resp.get("coupon_type_1_1");//代金券类型
            String coupon_refund_fee_1_0 = resp.get("coupon_refund_fee_1_0");//单个代金券退款金额
            String coupon_refund_fee_1_1 = resp.get("coupon_refund_fee_1_1");//单个代金券退款金额
            String refund_status_1 = resp.get("refund_status_1");//退款状态
            String refund_account_1 = resp.get("refund_account_1");//退款资金来源
            String refund_recv_accout_1 = resp.get("refund_recv_accout_1");//退款入账账户
            String refund_success_time_1 = resp.get("refund_success_time_1");//退款成功时间

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

    /**
     * 退款结果通知
     * 在申请退款接口中上传参数notify_url以开通该功能
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("refundNotify")
    @ResponseBody
    public String refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            InputStream is = request.getInputStream();

            /*ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[10 * 1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.flush();

            String xml = new String(baos.toByteArray(), "UTF-8");*/

            String xml = new String(IOUtils.toByteArray(is), "UTF-8");

            Map<String, String> resp = WXPayUtil.xmlToMap(xml);

            String returnCode = resp.get("return_code");
            String appId = resp.get("appid");
            String mchId = resp.get("mch_id");
            String reqInfo = resp.get("req_info");

            if (!success.equals(returnCode) ||
                    !wxpayConfig.getAppID().equals(appId) ||
                    !wxpayConfig.getMchID().equals(mchId)) {
                return failure;
            }

            String xml2 = AESUtil.decrypt(reqInfo);
            Map<String, String> resp2 = WXPayUtil.xmlToMap(xml2);

            String transactionId = resp2.get("transaction_id");
            String outTradeNo = resp2.get("out_trade_no");
            String outRefundNo = resp2.get("out_refund_no");
            String refundId = resp2.get("refund_id");

            String totalFee = resp2.get("total_fee");//订单总金额
            String refundFee = resp2.get("refund_fee");//申请退款金额

            String settlementTotalFee = resp2.get("settlement_total_fee");//应结订单金额
            String settlementRefundFee = resp2.get("settlement_refund_fee");//退款金额

            String refundStatus = resp2.get("refund_status");//退款状态
            String refundSuccessTime = resp2.get("success_time");//退款成功时间，竟然不是refund_success_time
            String refundAccount = resp2.get("refund_account");//退款资金来源
            String refundRecvAccout = resp2.get("refund_recv_accout");//退款入账账户
            String refundRequestSource = resp2.get("refund_request_source");//退款发起来源

            //文档里没有代金券信息

            return "<xml>" +
                    "<return_code><![CDATA[SUCCESS]]></return_code>" +
                    "<return_msg><![CDATA[OK]]></return_msg>" +
                    "</xml>";
        } catch (Exception e) {
            e.printStackTrace();
            return failure;
        }
    }

    /**
     * 对账单下载
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("downloadBill")
    @ResponseBody
    public ResponseBean downloadBill(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String billDate = request.getParameter("billDate");
            String billType = request.getParameter("billType");
            String tarType = request.getParameter("tarType");

            Map<String, String> params = new HashMap<>();
            params.put("bill_date", billDate);//账单日期
            params.put("bill_type", billType);//账单类型
            params.put("tar_type", tarType);//压缩账单

            Map<String, String> resp = wxpay.downloadBill(params);
            if (!success.equals(resp.get("return_code"))) {
                return ResponseBean.getFailure(failure, resp);
            }

            String data = resp.get("data");

            return ResponseBean.getSuccess(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseBean.getFailure(failure);
        }
    }

}
