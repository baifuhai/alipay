package com.test.alipay.controller;

import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.test.alipay.dao.TradeDao;
import com.test.alipay.model.Trade;
import com.test.alipay.service.AlipayService;
import com.test.alipay.util.AlipayUtil;
import com.test.alipay.util.ResponseBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

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

        //请求支付，返回form表单
        String result = alipayService.pagePay(outTradeNo, totalAmount, subject, body).getBody();

        //输出form表单
        writeHtml(response, result);
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
    public ResponseBean pagePayQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tradeNo = request.getParameter("WIDTQtrade_no");
        String outTradeNo = request.getParameter("WIDTQout_trade_no");

        //交易查询
        AlipayTradeQueryResponse resp = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp.getBody());
        }

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }
    @RequestMapping("pagePayQueryById")
    @ResponseBody
    public ResponseBean pagePayQueryById(Integer id) throws Exception {
        Trade trade = tradeDao.findOne(id);
        String tradeNo = trade.getTradeNo();
        String outTradeNo = trade.getOutTradeNo();

        //交易查询
        AlipayTradeQueryResponse resp = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp.getBody());
        }

        //更改订单状态
        trade.setTradeNo(resp.getTradeNo());
        trade.setStatus(resp.getTradeStatus());
        tradeDao.save(trade);

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }

    /**
     * 退款
     *
     * @param request
     * @param response
     */
    @RequestMapping("refund")
    @ResponseBody
    public ResponseBean refund(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tradeNo = request.getParameter("WIDTRtrade_no");
        String outTradeNo = request.getParameter("WIDTRout_trade_no");
        String refundAmount = request.getParameter("WIDTRrefund_amount");
        String refundReason = request.getParameter("WIDTRrefund_reason");
        String refundNo = request.getParameter("WIDTRout_request_no");

        //退款
        AlipayTradeRefundResponse resp = alipayService.refund(tradeNo, outTradeNo, refundAmount, refundReason, refundNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("退款失败", resp.getBody());
        }

        if (StringUtils.isBlank(refundNo)) {
            refundNo = outTradeNo;
        }

        //退款查询
        AlipayTradeFastpayRefundQueryResponse resp2 = alipayService.refundQuery(tradeNo, outTradeNo, refundNo);
        if (!resp2.isSuccess()) {
            return ResponseBean.getFailure("退款查询失败", resp2.getBody());
        }

        //交易查询
        AlipayTradeQueryResponse resp3 = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp3.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp3.getBody());
        }
        
        //更改订单状态
        Trade trade = tradeDao.findByOutTradeNo(outTradeNo);
        trade.setTradeNo(resp3.getTradeNo());
        trade.setStatus(resp3.getTradeStatus());
        tradeDao.save(trade);

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }
    @RequestMapping("refundById")
    @ResponseBody
    public ResponseBean refundById(Integer id) throws Exception {
        Trade trade = tradeDao.findOne(id);
        String tradeNo = trade.getTradeNo();
        String outTradeNo = trade.getOutTradeNo();
        String refundAmount = trade.getTotalAmount();
        String refundReason = "原因";
        String refundNo = null;

        //退款
        AlipayTradeRefundResponse resp = alipayService.refund(tradeNo, outTradeNo, refundAmount, refundReason, refundNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("退款失败", resp.getBody());
        }

        if (StringUtils.isBlank(refundNo)) {
            refundNo = outTradeNo;
        }

        //退款查询
        AlipayTradeFastpayRefundQueryResponse resp2 = alipayService.refundQuery(tradeNo, outTradeNo, refundNo);
        if (!resp2.isSuccess()) {
            return ResponseBean.getFailure("退款查询失败", resp2.getBody());
        }

        //交易查询
        AlipayTradeQueryResponse resp3 = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp3.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp3.getBody());
        }

        //更改订单状态
        trade.setTradeNo(resp3.getTradeNo());
        trade.setStatus(resp3.getTradeStatus());
        tradeDao.save(trade);

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }

    /**
     * 退款查询
     *
     * @param request
     * @param response
     */
    @RequestMapping("refundQuery")
    @ResponseBody
    public ResponseBean refundQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tradeNo = request.getParameter("WIDRQtrade_no");
        String outTradeNo = request.getParameter("WIDRQout_trade_no");
        String refundNo = request.getParameter("WIDRQout_request_no");

        if (StringUtils.isBlank(refundNo)) {
            refundNo = outTradeNo;
        }

        //退款查询
        AlipayTradeFastpayRefundQueryResponse resp = alipayService.refundQuery(tradeNo, outTradeNo, refundNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("退款查询失败", resp.getBody());
        }

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }
    @RequestMapping("refundQueryById")
    @ResponseBody
    public ResponseBean refundQueryById(Integer id) throws Exception {
        Trade trade = tradeDao.findOne(id);
        String tradeNo = trade.getTradeNo();
        String outTradeNo = trade.getOutTradeNo();
        String refundNo = trade.getRefundNo();

        if (StringUtils.isBlank(refundNo)) {
            refundNo = outTradeNo;
        }

        //退款查询
        AlipayTradeFastpayRefundQueryResponse resp = alipayService.refundQuery(tradeNo, outTradeNo, refundNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("退款查询失败", resp.getBody());
        }

        //输出
        return ResponseBean.getSuccess(resp.getBody());
    }

    /**
     * 关闭交易
     *
     * @param request
     * @param response
     */
    @RequestMapping("close")
    @ResponseBody
    public ResponseBean close(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tradeNo = request.getParameter("WIDTCtrade_no");
        String outTradeNo = request.getParameter("WIDTCout_trade_no");

        //关闭交易
        AlipayTradeCloseResponse resp = alipayService.close(tradeNo, outTradeNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("关闭交易失败", resp.getBody());
        }

        //交易查询
        AlipayTradeQueryResponse resp2 = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp2.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp2.getBody());
        }

        //更改订单状态
        Trade trade = tradeDao.findByOutTradeNo(outTradeNo);
        trade.setTradeNo(resp2.getTradeNo());
        trade.setStatus(resp2.getTradeStatus());
        tradeDao.save(trade);

        return ResponseBean.getSuccess(resp.getBody());
    }
    @RequestMapping("closeById")
    @ResponseBody
    public ResponseBean closeById(Integer id) throws Exception {
        Trade trade = tradeDao.findOne(id);
        String tradeNo = trade.getTradeNo();
        String outTradeNo = trade.getOutTradeNo();

        //关闭交易
        AlipayTradeCloseResponse resp = alipayService.close(tradeNo, outTradeNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("关闭交易失败", resp.getBody());
        }

        //交易查询
        AlipayTradeQueryResponse resp2 = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp2.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp2.getBody());
        }

        //更改订单状态
        trade.setTradeNo(resp2.getTradeNo());
        trade.setStatus(resp2.getTradeStatus());
        tradeDao.save(trade);

        return ResponseBean.getSuccess(resp.getBody());
    }

    /**
     * 同步通知页面
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping("returnUrl")
    @ResponseBody
    public ResponseBean returnUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //验签
        boolean signVerified = alipayService.signCheck(request);
        if (!signVerified) {
            return ResponseBean.getFailure("验签失败");
        }

        //开发者的应用ID
        String appId = request.getParameter("app_id");

        //收款支付宝账号对应的支付宝唯一用户号，以2088开头的纯16位数字
        String sellerId = request.getParameter("seller_id");

        //支付宝交易号
        String tradeNo = request.getParameter("trade_no");

        //商户订单号
        String outTradeNo = request.getParameter("out_trade_no");

        //付款金额
        String totalAmount = request.getParameter("total_amount");

        //判断appId和sellerId是否正确
        if (!AlipayUtil.APP_ID.equals(appId) || !AlipayUtil.SELLER_ID.equals(sellerId)) {
            return ResponseBean.getFailure("app_id或seller_id不正确");
        }

        //判断商户订单号是否存在
        Trade trade = tradeDao.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return ResponseBean.getFailure("商户订单号" + outTradeNo + "不存在");
        }

        //判断金额是否正确
        if (!trade.getTotalAmount().equals(totalAmount)) {
            return ResponseBean.getFailure("金额" + totalAmount + "不正确");
        }

        //交易查询
        AlipayTradeQueryResponse resp = alipayService.pagePayQuery(tradeNo, outTradeNo);
        if (!resp.isSuccess()) {
            return ResponseBean.getFailure("交易查询失败", resp.getBody());
        }

        //更改订单状态
        trade.setTradeNo(tradeNo);
        trade.setStatus(resp.getTradeStatus());
        tradeDao.save(trade);

        return ResponseBean.getSuccess("支付成功");
    }

    /**
     * 异步通知，成功则返回success
     * 支付宝POST过来反馈信息
     * 创建该页面文件时，请留心该页面文件中无任何HTML代码及空格
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("notifyUrl")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //验签
        boolean signVerified = alipayService.signCheck(request);
        if (!signVerified) {
            //调试用，写文本函数记录程序运行情况是否正常
            //String sWord = AlipaySignature.getSignCheckContentV1(params);
            //AlipayConfig.logResult(sWord);
            return "failure";
        }

        //商户订单号
        String outTradeNo = request.getParameter("out_trade_no");

        //支付宝交易号
        String tradeNo = request.getParameter("trade_no");

        //交易状态
        String tradeStatus = request.getParameter("trade_status");

        //判断该笔订单是否在商户网站中已经做过处理
        //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
        //如果有做过处理，不执行商户的业务程序
        if (tradeStatus.equals("TRADE_FINISHED")) {
            //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
        } else if (tradeStatus.equals("TRADE_SUCCESS")) {
            //付款完成后，支付宝系统发送该交易状态通知
        }

        //更改订单状态
        Trade trade = tradeDao.findByOutTradeNo(outTradeNo);
        trade.setTradeNo(tradeNo);
        trade.setStatus(tradeStatus);
        tradeDao.save(trade);

        return "success";
    }

    private void writeHtml(HttpServletResponse response, String result) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    @RequestMapping("deleteTrade")
    @ResponseBody
    public String deleteTrade(Integer id) {
        tradeDao.delete(id);
        return "success";
    }

}
