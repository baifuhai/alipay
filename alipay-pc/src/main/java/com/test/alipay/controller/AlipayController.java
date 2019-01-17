package com.test.alipay.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.google.gson.Gson;
import com.test.alipay.config.AlipayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequestMapping("alipay")
@Controller
public class AlipayController {

    @RequestMapping("pagePay")
    public void pagePay(HttpServletRequest request, HttpServletResponse response, String out_trade_no, String total_amount, String subject) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, AlipayConfig.format, AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("product_code", "FAST_INSTANT_TRADE_PAY");
        params.put("total_amount", total_amount);
        params.put("subject", subject);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //输出
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    @RequestMapping("pagePayQuery")
    public void pagePayQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, AlipayConfig.format, AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = request.getParameter("WIDTQout_trade_no");

        //支付宝交易号
        String trade_no = request.getParameter("WIDTQtrade_no");

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(trade_no) && StringUtils.isBlank(out_trade_no)) {
            params.put("trade_no", trade_no);
        } else if (StringUtils.isBlank(trade_no) && StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    @GetMapping("returnUrl")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /* *
         * 功能：支付宝服务器同步通知页面
         * 日期：2017-03-30
         * 说明：
         * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
         * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。


         *************************页面功能说明*************************
         * 该页面仅做页面展示，业务逻辑处理请勿在该页面执行
         */

        //获取支付宝GET过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名

        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();

        //——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            out.println("trade_no:"+trade_no+"<br/>out_trade_no:"+out_trade_no+"<br/>total_amount:"+total_amount);
        }else {
            out.println("验签失败");
        }
        //——请在这里编写您的程序（以上代码仅作参考）——

        out.flush();
        out.close();
    }

}
