package com.test.unionpay.controller;

import com.unionpay.acp.demo.DemoBase;
import com.unionpay.acp.sdk.AcpService;
import com.unionpay.acp.sdk.LogUtil;
import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKConstants;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//@RequestMapping("unionpay")
@Controller
public class UnionpayController {

    /**
     * 测试过程中的如果遇到疑问或问题您可以：
     * 1）优先在open平台中查找答案：
     *      调试过程中的问题或其他问题请在 https://open.unionpay.com/ajweb/help/faq/list 帮助中心 FAQ 搜索解决方案
     *      测试过程中产生的6位应答码问题疑问请在https://open.unionpay.com/ajweb/help/respCode/respCodeList 输入应答码搜索解决方案
     * 2）咨询在线人工支持：
     *      open.unionpay.com注册一个用户并登陆在右上角点击“在线客服”，咨询人工QQ测试支持。
     */

    /**
     * 消费：前台跳转，有前台通知应答和后台通知应答
     * 提示：该接口参考文档位置：
     * open.unionpay.com帮助中心 下载  产品接口规范  《网关支付产品接口规范》
     * 《平台接入接口规范-第5部分-附录》（内包含应答码接口规范，全渠道平台银行名称-简码对照表)
     *  《全渠道平台接入接口规范 第3部分 文件接口》（对账文件格式说明）
     *
     * 交易说明:
     * 1）以后台通知或交易状态查询交易确定交易成功，前台通知不能作为判断成功的标准
     * 2）交易状态查询交易（Form_6_5_Query）建议调用机制：前台类交易建议间隔（5分、10分、30分、60分、120分）发起交易查询，如果查询到结果成功，则不用再查询。
     * （失败，处理中，查询不到订单均可能为中间状态）。也可以建议商户使用payTimeout（支付超时时间），过了这个时间点查询，得到的结果为最终结果。
     */
    @RequestMapping("form_6_2_FrontConsume")
    public void form_6_2_FrontConsume(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.config;

        String orderId = req.getParameter("orderId");
        String txnAmt = req.getParameter("txnAmt");
        String txnTime = req.getParameter("txnTime");

        Map<String, String> requestData = new HashMap<>();

        //银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        requestData.put("version", config.getVersion());   			  //版本号，全渠道默认值
        requestData.put("encoding", config.getEncoding()); 			  //字符集编码，可以使用UTF-8,GBK两种方式
        requestData.put("signMethod", config.getSignMethod());        //签名方法
        requestData.put("txnType", "01");               			  //交易类型 ，01：消费
        requestData.put("txnSubType", "01");            			  //交易子类型， 01：自助消费
        requestData.put("bizType", "000201");           			  //业务类型，B2C网关支付，手机wap支付
        requestData.put("channelType", "07");           			  //渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机

        //商户接入参数
        requestData.put("merId", config.getMerId());    //商户号码，请改成自己申请的正式商户号或者open上注册得来的777测试商户号
        requestData.put("accessType", "0");             //接入类型，0：直连商户
        requestData.put("orderId", orderId);            //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
        requestData.put("txnAmt", txnAmt);             	//交易金额，单位分，不要带小数点
        requestData.put("txnTime", txnTime);            //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        requestData.put("currencyCode", "156");         //交易币种（境内商户一般是156 人民币）
        //requestData.put("reqReserved", "透传字段");    //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。

        requestData.put("riskRateInfo", "{commodityName=测试商品名称}");

        //前台通知地址（需设置为外网能访问 http https均可），支付成功后的页面，点击“返回商户”按钮的时候将异步通知报文post到该地址
        //如果想要实现过几秒中自动跳转回商户页面权限，需联系银联业务申请开通自动返回商户权限
        //异步通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        requestData.put("frontUrl", config.getFrontUrl());

        //后台通知地址（需设置为外网能访问 http https均可），支付成功后，银联会自动将异步通知报文post到商户上送的该地址，失败的交易银联不会发送后台通知
        //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        //注意:
        // 1.需设置为外网能访问，否则收不到通知
        // 2.http https均可
        // 3.收单后台通知后需要10秒内返回http200或302状态码
        // 4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟
        // 5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
        requestData.put("backUrl", config.getBackUrl());

        // 订单超时时间
        // 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。 跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户。
        // 此时间建议取支付时的北京时间加15分钟。
        // 超过超时时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败。
        requestData.put("payTimeout", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime() + 15 * 60 * 1000));

        //////////////////////////////////////////////////
        //
        // 报文中特殊用法请查看PCwap网关跳转支付特殊用法.txt
        //
        //////////////////////////////////////////////////

        //请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面
        Map<String, String> submitFromData = AcpService.sign(requestData, config.getEncoding());
        //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String requestFrontUrl = config.getFrontTransUrl();

        //生成自动跳转的Html表单
        String html = AcpService.createAutoFormHtml(requestFrontUrl, submitFromData, config.getEncoding());
        LogUtil.writeLog("请求HTML：" + html);

        //将生成的html写到浏览器中完成自动跳转打开银联支付页面
        resp.setContentType("text/html; charset=" + config.getEncoding());
        resp.getWriter().write(html);
    }

    /**
     * 后台通知接收
     *
     * 该接口参考文档位置：
     * open.unionpay.com帮助中心 下载  产品接口规范  《网关支付产品接口规范》，<br>
     * 《平台接入接口规范-第5部分-附录》（内包含应答码接口规范，全渠道平台银行名称-简码对照表），
     *
     * 交易说明：
     * 前台类交易成功才会发送后台通知。后台类交易（有后台通知的接口）交易结束之后成功失败都会发通知。
     * 为保证安全，涉及资金类的交易，收到通知后请再发起查询接口确认交易成功。不涉及资金的交易可以以通知接口respCode=00判断成功。
     * 未收到通知时，查询接口调用时间点请参照此FAQ：https://open.unionpay.com/ajweb/help/faq/list?id=77&level=0&from=0
     */
    @RequestMapping("backRcvResponse")
    public void backRcvResponse(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LogUtil.writeLog("BackRcvResponse接收后台通知开始");

        String encoding = req.getParameter(SDKConstants.param_encoding);
        // 获取银联通知服务器发送的后台通知参数
        Map<String, String> reqParam = getAllRequestParam(req);
        LogUtil.printRequestLog(reqParam);

        //重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
        if (!AcpService.validate(reqParam, encoding)) {
            LogUtil.writeLog("验证签名结果[失败].");
            //验签失败，需解决验签问题

        } else {
            LogUtil.writeLog("验证签名结果[成功].");
            //【注：为了安全验签成功才应该写商户的成功处理逻辑】交易成功，更新商户订单状态

            String orderId =reqParam.get("orderId"); //获取后台通知的数据，其他字段也可用类似方式获取
            String respCode = reqParam.get("respCode");
            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。

        }
        LogUtil.writeLog("BackRcvResponse接收后台通知结束");
        //返回给银联服务器http 200  状态码
        resp.getWriter().print("ok");
    }

    /**
     * 获取请求参数中所有的信息
     * 当商户上送frontUrl或backUrl地址中带有参数信息的时候，
     * 这种方式会将url地址中的参数读到map中，会导多出来这些信息从而致验签失败，这个时候可以自行修改过滤掉url中的参数或者使用getAllRequestParamStream方法。
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                // 在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                if (res.get(en) == null || "".equals(res.get(en))) {
                    // System.out.println("======为空的字段名===="+en);
                    res.remove(en);
                }
            }
        }
        return res;
    }

    /**
     * 获取请求参数中所有的信息。
     * 非struts可以改用此方法获取，好处是可以过滤掉request.getParameter方法过滤不掉的url中的参数。
     * struts可能对某些content-type会提前读取参数导致从inputstream读不到信息，所以可能用不了这个方法。理论应该可以调整struts配置使不影响，但请自己去研究。
     * 调用本方法之前不能调用req.getParameter("key");这种方法，否则会导致request取不到输入流。
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParamStream(final HttpServletRequest request) {
        SDKConfig config = SDKConfig.config;

        Map<String, String> res = new HashMap<String, String>();
        try {
            String notifyStr = new String(IOUtils.toByteArray(request.getInputStream()), config.getEncoding());
            LogUtil.writeLog("收到通知报文：" + notifyStr);
            String[] kvs= notifyStr.split("&");
            for(String kv : kvs){
                String[] tmp = kv.split("=");
                if(tmp.length >= 2){
                    String key = tmp[0];
                    String value = URLDecoder.decode(tmp[1], config.getEncoding());
                    res.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LogUtil.writeLog("getAllRequestParamStream.UnsupportedEncodingException error: " + e.getClass() + ":" + e.getMessage());
        } catch (IOException e) {
            LogUtil.writeLog("getAllRequestParamStream.IOException error: " + e.getClass() + ":" + e.getMessage());
        }
        return res;
    }

    /**
     * 前台通知接收处理
     *
     * 该接口参考文档位置：
     * open.unionpay.com帮助中心 下载  产品接口规范  《网关支付产品接口规范》
     * 《平台接入接口规范-第5部分-附录》（内包含应答码接口规范，全渠道平台银行名称-简码对照表），
     *
     * 交易说明：
     * 支付成功点击“返回商户”按钮的时候出现的处理页面示例
     * 为保证安全，涉及资金类的交易，收到通知后请再发起查询接口确认交易成功。不涉及资金的交易可以以通知接口respCode=00判断成功。
     * 未收到通知时，查询接口调用时间点请参照此FAQ：https://open.unionpay.com/ajweb/help/faq/list?id=77&level=0&from=0
     */
    @RequestMapping("frontRcvResponse")
    public void frontRcvResponse(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LogUtil.writeLog("FrontRcvResponse前台接收报文返回开始");

        SDKConfig config = SDKConfig.config;

        String encoding = req.getParameter(SDKConstants.param_encoding);
        LogUtil.writeLog("返回报文中encoding=[" + encoding + "]");
        String pageResult = "";
        if (config.getEncoding().equalsIgnoreCase(encoding)) {
            pageResult = "/utf8_result.jsp";
        } else {
            pageResult = "/gbk_result.jsp";
        }
        Map<String, String> respParam = getAllRequestParam(req);

        // 打印请求报文
        LogUtil.printRequestLog(respParam);

        Map<String, String> valideData = null;
        StringBuffer page = new StringBuffer();
        if (null != respParam && !respParam.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = respParam.entrySet()
                    .iterator();
            valideData = new HashMap<String, String>(respParam.size());
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                value = new String(value.getBytes(encoding), encoding);
                page.append("<tr><td width=\"30%\" align=\"right\">" + key
                        + "(" + key + ")</td><td>" + value + "</td></tr>");
                valideData.put(key, value);
            }
        }
        if (!AcpService.validate(valideData, encoding)) {
            page.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>失败</td></tr>");
            LogUtil.writeLog("验证签名结果[失败].");
        } else {
            page.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>成功</td></tr>");
            LogUtil.writeLog("验证签名结果[成功].");
            System.out.println(valideData.get("orderId")); //其他字段也可用类似方式获取

            String respCode = valideData.get("respCode");
            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
        }
        req.setAttribute("result", page.toString());
        req.getRequestDispatcher(pageResult).forward(req, resp);

        LogUtil.writeLog("FrontRcvResponse前台接收报文返回结束");
    }

//    @RequestMapping("xxx")
//    public void xxx(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//
//    }
//
//    @RequestMapping("xxx")
//    public void xxx(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//
//    }
//
//    @RequestMapping("xxx")
//    public void xxx(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//
//    }

}
