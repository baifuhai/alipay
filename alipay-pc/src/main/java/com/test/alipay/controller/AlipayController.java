package com.test.alipay.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.alipay.config.AlipayConfig;
import com.test.alipay.dao.TradeDao;
import com.test.alipay.model.Trade;
import com.test.alipay.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequestMapping("alipay")
@Controller
public class AlipayController {
    
    @Autowired
    AlipayService alipayService;
    
    @Autowired
    TradeDao tradeDao;
    
    /**
     * 支付
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("pagePay")
    public void pagePay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String outTradeNo = request.getParameter("WIDout_trade_no");
        String totalAmount = request.getParameter("WIDtotal_amount");
        String subject = request.getParameter("WIDsubject");
        String body = request.getParameter("WIDbody");

        //本地保存
        Trade trade = new Trade();
        trade.setOutTradeNo(outTradeNo);
        trade.setTotalAmount(totalAmount);
        trade.setSubject(subject);
        trade.setBody(body);
        tradeDao.save(trade);

        //请求
        String result = alipayService.pagePay(outTradeNo, totalAmount, subject, body);

        //输出
        write(response, result);
    }

    /**
     * 交易查询
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("pagePayQuery")
    @ResponseBody
    public String pagePayQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tradeNo = request.getParameter("WIDTQtrade_no");
        String outTradeNo = request.getParameter("WIDTQout_trade_no");

        //请求
        String result = alipayService.pagePayQuery(tradeNo, outTradeNo);

        //输出
        return result;
    }

    /**
     * 退款
     *
     * @param request
     * @param response
     */
    @RequestMapping("refund")
    @ResponseBody
    public String refund(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = request.getParameter("WIDTRout_trade_no");
        //支付宝交易号
        String trade_no = request.getParameter("WIDTRtrade_no");
        //请二选一设置
        //需要退款的金额，该金额不能大于订单金额，必填
        String refund_amount = request.getParameter("WIDTRrefund_amount");
        //退款的原因说明
        String refund_reason = request.getParameter("WIDTRrefund_reason");
        //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
        String out_request_no = request.getParameter("WIDTRout_request_no");

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", trade_no);
        params.put("out_trade_no", out_trade_no);
        params.put("refund_amount", refund_amount);
        params.put("refund_reason", refund_reason);
        params.put("out_request_no", out_request_no);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        return result;
    }

    /**
     * 退款查询
     *
     * @param request
     * @param response
     */
    @RequestMapping("refundQuery")
    @ResponseBody
    public String refundQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = request.getParameter("WIDRQout_trade_no");
        //支付宝交易号
        String trade_no = request.getParameter("WIDRQtrade_no");
        //请二选一设置
        //请求退款接口时，传入的退款请求号，如果在退款请求时未传入，则该值为创建交易时的外部交易号，必填
        String out_request_no = request.getParameter("WIDRQout_request_no");

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", trade_no);
        params.put("out_trade_no", out_trade_no);
        params.put("out_request_no", out_request_no);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        return result;
    }

    /**
     * 交易关闭
     *
     * @param request
     * @param response
     */
    @RequestMapping("close")
    @ResponseBody
    public String close(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = request.getParameter("WIDTCout_trade_no");
        //支付宝交易号
        String trade_no = request.getParameter("WIDTCtrade_no");
        //请二选一设置

        Map<String, Object> params = new HashMap<>();
        params.put("trade_no", trade_no);
        params.put("out_trade_no", out_trade_no);

        alipayRequest.setBizContent(new Gson().toJson(params));

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        return result;
    }

    /**
     * 同步通知页面
     *
     * @param request
     * @param response
     * @throws Exception
     */
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
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
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

        String result = null;

        //——请在这里编写您的程序（以下代码仅作参考）——
        if(signVerified) {
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            String s = alipayService.pagePayQuery(trade_no, out_trade_no);
            Map<String, Object> map = new Gson().fromJson(s, new TypeToken<Map<String, Object>>(){}.getType());
            Map<String, Object> map2 = (Map<String, Object>) map.get("alipay_trade_query_response");

            //panduan
            if (true) {
                result = "failure";
            }

            Trade trade = tradeDao.findByOutTradeNo(out_trade_no);
            trade.setTradeNo(trade_no);
            trade.setStatus(map2.get("trade_status").toString());
            tradeDao.save(trade);

            result = "trade_no:" + trade_no + "<br/>out_trade_no:" + out_trade_no + "<br/>total_amount:" + total_amount;
        }else {
            result = "验签失败";
        }
        //——请在这里编写您的程序（以上代码仅作参考）——

        write(response, result);
    }

    /**
     * 异步通知，成功则返回success
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("notifyUrl")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /* *
         * 功能：支付宝服务器异步通知页面
         * 日期：2017-03-30
         * 说明：
         * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
         * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。


         *************************页面功能说明*************************
         * 创建该页面文件时，请留心该页面文件中无任何HTML代码及空格。
         * 该页面不能在本机电脑测试，请到服务器上做测试。请确保外部可以访问该页面。
         * 如果没有收到该页面返回的 success
         * 建议该页面只做支付成功的业务逻辑处理，退款的处理请以调用退款查询接口的结果为准。
         */

        //获取支付宝POST过来反馈信息
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

        //——请在这里编写您的程序（以下代码仅作参考）——

        /* 实际验证过程建议商户务必添加以下校验：
        1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        4、验证app_id是否为该商户本身。
	    */

        String result = null;

        if(signVerified) {//验证成功
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

            if(trade_status.equals("TRADE_FINISHED")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            }else if (trade_status.equals("TRADE_SUCCESS")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //付款完成后，支付宝系统发送该交易状态通知
            }

            result = "success";

        }else {//验证失败
            result = "fail";

            //调试用，写文本函数记录程序运行情况是否正常
            //String sWord = AlipaySignature.getSignCheckContentV1(params);
            //AlipayConfig.logResult(sWord);
        }

        //——请在这里编写您的程序（以上代码仅作参考）——

        return result;
    }

    private void write(HttpServletResponse response, String result) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

}
