package com.test.unionpay.controller;

import com.unionpay.acp.demo.DemoBase;
import com.unionpay.acp.sdk.AcpService;
import com.unionpay.acp.sdk.LogUtil;
import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

//@RequestMapping("unionpay")
@Controller
public class UnionpayController {

    @Autowired
    SDKConfig config;

    /**
     * 日期： 2015-09<br>
     * 测试过程中的如果遇到疑问或问题您可以：
     * 1）优先在open平台中查找答案：
     *      调试过程中的问题或其他问题请在 https://open.unionpay.com/ajweb/help/faq/list 帮助中心 FAQ 搜索解决方案
     *      测试过程中产生的6位应答码问题疑问请在https://open.unionpay.com/ajweb/help/respCode/respCodeList 输入应答码搜索解决方案
     * 2）咨询在线人工支持：
     *      open.unionpay.com注册一个用户并登陆在右上角点击“在线客服”，咨询人工QQ测试支持。
     *
     * 该接口参考文档位置：
     * open.unionpay.com帮助中心 下载  产品接口规范  《网关支付产品接口规范》
     * 《平台接入接口规范-第5部分-附录》（内包含应答码接口规范，全渠道平台银行名称-简码对照表）
     * 《全渠道平台接入接口规范 第3部分 文件接口》（对账文件格式说明）
     */

    /**
     * 消费
     * 前台跳转，有前台通知应答和后台通知应答
     *
     * 1）以后台通知或交易状态查询交易确定交易成功，前台通知不能作为判断成功的标准
     * 2）交易状态查询交易（Form_6_5_Query）建议调用机制：前台类交易建议间隔（5分、10分、30分、60分、120分）发起交易查询，如果查询到结果成功，则不用再查询。
     * （失败，处理中，查询不到订单均可能为中间状态）。也可以建议商户使用payTimeout（支付超时时间），过了这个时间点查询，得到的结果为最终结果。
     * 3）报文中特殊用法请查看PCwap网关跳转支付特殊用法.txt
     */
    @RequestMapping("form_6_2_FrontConsume")
    public void form_6_2_FrontConsume(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String orderId = req.getParameter("orderId");
        String txnAmt = req.getParameter("txnAmt");
        String txnTime = req.getParameter("txnTime");

        Map<String, String> requestData = new HashMap<>();

        //银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        requestData.put("version", config.getVersion());   			  //版本号
        requestData.put("encoding", config.getEncoding()); 			  //字符集编码，可以使用UTF-8、GBK两种方式
        requestData.put("bizType", "000201");           			  //业务类型，B2C网关支付，手机wap支付
        requestData.put("txnType", "01");               			  //交易类型，01：消费
        requestData.put("txnSubType", "01");            			  //交易子类型，01：自助消费
        requestData.put("signMethod", config.getSignMethod());        //签名方法
        requestData.put("channelType", "07");           			  //渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC、平板；08：手机

        //商户接入参数
        requestData.put("merId", config.getMerId());    //商户代码，请改成自己申请的正式商户号或者open上注册得来的777测试商户号
        requestData.put("orderId", orderId);            //商户订单号，8-40位数字字母，不能含“-”或“_”
        requestData.put("txnTime", txnTime);            //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        requestData.put("txnAmt", txnAmt);             	//交易金额，单位分，不要带小数点
        requestData.put("currencyCode", "156");         //交易币种（境内商户一般是156 人民币）
        requestData.put("accessType", "0");             //接入类型，0：直连商户，1：收单机构接入，2：平台商户接入

        // 前台通知地址，按条件必填
        // （需设置为外网能访问http https均可），支付成功后的页面，点击“返回商户”按钮的时候将异步通知报文post到该地址
        // 如果想要实现过几秒中自动跳转回商户页面权限，需联系银联业务申请开通自动返回商户权限
        // 异步通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        requestData.put("frontUrl", config.getFrontUrl());

        // 后台通知地址，必填
        // （需设置为外网能访问http https均可），支付成功后，银联会自动将异步通知报文post到商户上送的该地址，失败的交易银联不会发送后台通知
        // 后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        // 注意:
        // 3.收单后台通知后需要10秒内返回http200或302状态码
        // 4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟
        // 5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
        requestData.put("backUrl", config.getBackUrl());

        // 订单支付超时时间，选填
        // 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户
        // 此时间建议取支付时的北京时间加15分钟
        // 超过此时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败
        requestData.put("payTimeout", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime() + 15 * 60 * 1000));

        // 请求方保留域，选填
        // 透传字段（可以实现商户自定义参数的追踪）本交易的后台通知，对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。
        // 出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        //requestData.put("reqReserved", "透传字段");

        // 风控信息域，选填
        requestData.put("riskRateInfo", "{commodityName=测试商品名称}");

        //请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面
        //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        Map<String, String> submitFromData = AcpService.sign(requestData, config.getEncoding());

        //生成自动跳转的Html表单
        String html = AcpService.createAutoFormHtml(config.getFrontTransUrl(), submitFromData, config.getEncoding());

        //将生成的html写到浏览器中完成自动跳转打开银联支付页面
        resp.setContentType("text/html; charset=" + config.getEncoding());
        resp.getWriter().write(html);
    }

    /**
     * 前台通知
     *
     * 支付成功点击“返回商户”按钮的时候出现的处理页面示例
     * 为保证安全，涉及资金类的交易，收到通知后请再发起查询接口确认交易成功。不涉及资金的交易可以以通知接口respCode=00判断成功。
     * 未收到通知时，查询接口调用时间点请参照此FAQ：https://open.unionpay.com/ajweb/help/faq/list?id=77&level=0&from=0
     */
    @RequestMapping("frontRcvResponse")
    public void frontRcvResponse(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String encoding = req.getParameter(SDKConstants.param_encoding);

        Map<String, String> reqParam = getAllRequestParam(req);

        StringBuilder sb = new StringBuilder();

        Set<Map.Entry<String, String>> es = reqParam.entrySet();
        for (Map.Entry<String, String> e : es) {
            String key = e.getKey();
            String value = e.getValue();
            //value = new String(value.getBytes(encoding), encoding);
            sb.append("<tr><td width=\"30%\" align=\"right\">" + key + "(" + key + ")</td><td>" + value + "</td></tr>");
        }

        if (!AcpService.validate(reqParam, encoding)) {
            sb.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>失败</td></tr>");
        } else {
            sb.append("<tr><td width=\"30%\" align=\"right\">验证签名结果</td><td>成功</td></tr>");

            String respCode = reqParam.get("respCode");//应答码
            String respMsg = reqParam.get("respMsg");//应答信息
            String merId = reqParam.get("merId");//商户代码
            String orderId = reqParam.get("orderId");//商户订单号
            String queryId = reqParam.get("queryId");//查询流水号
            String signMethod = reqParam.get("signMethod");//签名方法
            String accNo = reqParam.get("accNo");//账号
            String payType = reqParam.get("payType");//支付方式
            String payCardType = reqParam.get("payCardType");//支付卡类型
            String txnTime = reqParam.get("txnTime");//订单发送时间
            String txnAmt = reqParam.get("txnAmt");//交易金额
            String txnType = reqParam.get("txnType");//交易类型
            String txnSubType = reqParam.get("txnSubType");//交易子类型
            String reqReserved = reqParam.get("reqReserved");//请求方保留域

            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
            if ("00".equals(respCode) || "A6".equals(respCode)) {

            }
        }

        req.setAttribute("result", sb.toString());

        String pageResult;
        if ("utf-8".equalsIgnoreCase(encoding)) {
            pageResult = "/utf8_result.jsp";
        } else {
            pageResult = "/gbk_result.jsp";
        }

        req.getRequestDispatcher(pageResult).forward(req, resp);
    }

    /**
     * 后台通知
     *
     * 前台类交易成功才会发送后台通知。后台类交易（有后台通知的接口）交易结束之后成功失败都会发通知。
     * 为保证安全，涉及资金类的交易，收到通知后请再发起查询接口确认交易成功。不涉及资金的交易可以以通知接口respCode=00判断成功。
     * 未收到通知时，查询接口调用时间点请参照此FAQ：https://open.unionpay.com/ajweb/help/faq/list?id=77&level=0&from=0
     */
    @RequestMapping("backRcvResponse")
    @ResponseBody
    public String backRcvResponse(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String encoding = req.getParameter(SDKConstants.param_encoding);

        Map<String, String> reqParam = getAllRequestParam(req);

        if (!AcpService.validate(reqParam, encoding)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "error";
        }

        String respCode = reqParam.get("respCode");//应答码
        String respMsg = reqParam.get("respMsg");//应答信息
        String merId = reqParam.get("merId");//商户代码
        String orderId = reqParam.get("orderId");//商户订单号
        String queryId = reqParam.get("queryId");//查询流水号
        String signMethod = reqParam.get("signMethod");//签名方法
        String accNo = reqParam.get("accNo");//账号
        String payType = reqParam.get("payType");//支付方式
        String payCardType = reqParam.get("payCardType");//支付卡类型
        String txnTime = reqParam.get("txnTime");//订单发送时间
        String txnAmt = reqParam.get("txnAmt");//交易金额
        String txnType = reqParam.get("txnType");//交易类型
        String txnSubType = reqParam.get("txnSubType");//交易子类型
        String reqReserved = reqParam.get("reqReserved");//请求方保留域

        String traceTime = reqParam.get("traceTime");//	交易传输时间
        String settleCurrencyCode = reqParam.get("settleCurrencyCode");//清算币种
        String settleAmt = reqParam.get("settleAmt");//清算金额
        String settleDate = reqParam.get("settleDate");//清算日期
        String traceNo = reqParam.get("traceNo");//系统跟踪号

        //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库
        if ("00".equals(respCode) || "A6".equals(respCode)) {

        }

        //返回给银联200或302状态码，银联判定为通知成功，其他返回码为通知失败
        resp.setStatus(HttpServletResponse.SC_OK);
        return "ok";
    }

    /**
     * 获取请求参数中所有的信息
     * 当商户上送frontUrl或backUrl地址中带有参数信息的时候，
     * 这种方式会将url地址中的参数读到map中，会导多出来这些信息从而致验签失败，这个时候可以自行修改过滤掉url中的参数或者使用getAllRequestParamStream方法
     *
     * @param request
     * @return
     */
    public Map<String, String> getAllRequestParam(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();

        Enumeration<String> names = request.getParameterNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String value = request.getParameter(name);
                if (StringUtils.isNotBlank(value)) {
                    res.put(name, value);
                }
            }
        }

        return res;
    }

    /**
     * 获取请求参数中所有的信息
     * 非struts可以改用此方法获取，好处是可以过滤掉request.getParameter方法过滤不掉的url中的参数
     * struts可能对某些content-type会提前读取参数导致从inputstream读不到信息，所以可能用不了这个方法。理论应该可以调整struts配置使不影响，但请自己去研究
     * 调用本方法之前不能调用req.getParameter("key")，这种方法，否则会导致request取不到输入流
     *
     * @param request
     * @return
     */
    public Map<String, String> getAllRequestParamStream(HttpServletRequest request) throws Exception {
        Map<String, String> res = new HashMap<>();

        String notifyStr = new String(IOUtils.toByteArray(request.getInputStream()), config.getEncoding());

        String[] kvs = notifyStr.split("&");
        for (String kv : kvs) {
            String[] tmp = kv.split("=");
            if (tmp.length >= 2) {
                String key = tmp[0];
                String value = URLDecoder.decode(tmp[1], config.getEncoding());
                res.put(key, value);
            }
        }

        return res;
    }

    /**
     * 交易状态查询
     *
     * 1）对前台交易发起交易状态查询：
     *      前台类交易建议间隔（5分、10分、30分、60分、120分）发起交易查询，如果查询到结果成功，则不用再查询。（失败，处理中，查询不到订单均可能为中间状态）。也可以建议商户使用payTimeout（支付超时时间），过了这个时间点查询，得到的结果为最终结果。
     * 2）对后台交易发起交易状态查询：
     *      后台类资金类交易同步返回00，成功银联有后台通知，商户也可以发起 查询交易，可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）。
     *      后台类资金类同步返03 04 05响应码及未得到银联响应（读超时）需发起查询交易，可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）。
     */
    @RequestMapping("form_6_5_Query")
    public void form_6_5_Query(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.config;

        String orderId = req.getParameter("orderId");
        String txnTime = req.getParameter("txnTime");

        Map<String, String> data = new HashMap<>();

        // 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        data.put("version", config.getVersion());                 //版本号
        data.put("encoding", config.getEncoding());               //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("bizType", "000201");                         //业务类型 B2C网关支付，手机wap支付
        data.put("txnType", "00");                             //交易类型 00-默认
        data.put("txnSubType", "00");                          //交易子类型  默认00
        data.put("signMethod", config.getSignMethod()); //签名方法

        // 商户接入参数
        data.put("merId", "777290058110048");                  //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("orderId", orderId);                 //商户订单号，每次发交易测试需修改为被查询的交易的订单号
        data.put("txnTime", txnTime);                 //订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间
        data.put("accessType", "0");                           //接入类型，商户接入固定填0，不需修改

        // 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
        // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        Map<String, String> reqData = AcpService.sign(data, config.getEncoding());

        //这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String, String> rspData = AcpService.post(reqData, config.getSingleQueryUrl(), config.getEncoding());

        if (rspData.isEmpty()) {
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        } else {
            if (!AcpService.validate(rspData, config.getEncoding())) {
                LogUtil.writeErrorLog("验证签名失败");
            } else {
                if ("00".equals(rspData.get("respCode"))) {//如果查询交易成功
                    //处理被查询交易的应答码逻辑
                    String origRespCode = rspData.get("origRespCode");
                    if ("00".equals(origRespCode)) {
                        //交易成功，更新商户订单状态
                    } else if("03".equals(origRespCode) || "04".equals(origRespCode) || "05".equals(origRespCode)) {
                        //需再次发起交易状态查询交易
                    } else {
                        //其他应答码为失败请排查原因
                    }
                } else {//查询交易本身失败，或者未查到原交易，检查查询交易报文要素
                }
            }
        }

        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);

        resp.getWriter().write("</br>请求报文:<br/>" + reqMessage + "<br/>" + "应答报文:</br>" + rspMessage);
    }

    /**
     * 消费撤销
     * 后台资金类交易，有同步应答和后台通知应答
     *
     * 交易说明:
     * 1）以后台通知或交易状态查询交易（Form_6_5_Query）确定交易成功，建议发起查询交易的机制：可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）
     * 2）消费撤销仅能对当清算日的消费做，必须为全额，一般当日或第二日到账。
     */
    @RequestMapping("form_6_3_ConsumeUndo")
    public void form_6_3_ConsumeUndo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.config;

        String origQryId = req.getParameter("origQryId");
        String txnAmt = req.getParameter("txnAmt");

        Map<String, String> data = new HashMap<>();

        // 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        data.put("version", config.getVersion());            //版本号
        data.put("encoding", config.getEncoding());          //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("bizType", "000201");                    //业务类型 B2C网关支付，手机wap支付
        data.put("txnType", "31");                        //交易类型 31-消费撤销
        data.put("txnSubType", "00");                     //交易子类型  默认00
        data.put("signMethod", config.getSignMethod()); //签名方法
        data.put("channelType", "07");                    //渠道类型，07-PC，08-手机

        // 商户接入参数
        data.put("merId", "777290058110048");             //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("orderId", DemoBase.getOrderId());       //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", DemoBase.getCurrentTime());   //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("txnAmt", txnAmt);                       //【撤销金额】，消费撤销时必须和原消费金额相同
        data.put("accessType", "0");                      //接入类型，商户接入固定填0，不需修改
        data.put("origQryId", origQryId);   			  //【原始交易流水号】，原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取
        data.put("currencyCode", "156");                  //交易币种(境内商户一般是156 人民币)
        //data.put("reqReserved", "透传信息");             //请求方保留域，，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        data.put("backUrl", config.getBackUrl());         //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费撤销交易 商户通知,其他说明同消费交易的商户通知

        // 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
        // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        Map<String, String> reqData  = AcpService.sign(data, config.getEncoding());

        // 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）
        // 这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String,String> rspData = AcpService.post(reqData, config.getBackTransUrl(), config.getEncoding());

        // 对应答码的处理，请根据您的业务逻辑来编写程序，以下应答码处理逻辑仅供参考

        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if (rspData.isEmpty()) {
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        } else {
            if (!AcpService.validate(rspData, config.getEncoding())) {
                LogUtil.writeErrorLog("验证签名失败");
            } else {
                String respCode = rspData.get("respCode");//应答码
                String respMsg = rspData.get("respMsg");//应答信息
                String merId = rspData.get("merId");//商户代码
                String orderId = rspData.get("orderId");//商户订单号
                String queryId = rspData.get("queryId");//查询流水号
                String origQryId2 = rspData.get("origQryId");//原交易查询流水号
                String txnTime = rspData.get("txnTime");//订单发送时间
                String txnAmt2 = rspData.get("txnAmt");//交易金额
                String txnType = rspData.get("txnType");//交易类型
                String txnSubType = rspData.get("txnSubType");//交易子类型

                if ("00".equals(respCode)) {
                    //交易已受理(不代表交易已成功），等待接收后台通知确定交易成功，也可以主动发起查询交易确定交易状态
                    System.out.println("respCode = 00");
                } else if("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {
                    //后续需发起交易状态查询交易确定交易状态
                } else {
                    //其他应答码为失败请排查原因
                }
            }
        }

        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);

        resp.getWriter().write("</br>请求报文:<br/>" + reqMessage + "<br/>" + "应答报文:</br>" + rspMessage);
    }

    /**
     * 消费撤销，后台通知
     */
    @RequestMapping("form_6_3_ConsumeUndo_notify")
    @ResponseBody
    public String form_6_3_ConsumeUndo_notify(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String encoding = req.getParameter(SDKConstants.param_encoding);

        Map<String, String> reqParam = getAllRequestParam(req);

        if (!AcpService.validate(reqParam, encoding)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "error";
        }

        String respCode = reqParam.get("respCode");//应答码
        String respMsg = reqParam.get("respMsg");//应答信息
        String merId = reqParam.get("merId");//商户代码
        String orderId = reqParam.get("orderId");//商户订单号
        String queryId = reqParam.get("queryId");//查询流水号
        String signMethod = reqParam.get("signMethod");//签名方法
        String txnTime = reqParam.get("txnTime");//订单发送时间
        String txnAmt = reqParam.get("txnAmt");//交易金额
        String txnType = reqParam.get("txnType");//交易类型
        String txnSubType = reqParam.get("txnSubType");//交易子类型
        String reqReserved = reqParam.get("reqReserved");//请求方保留域

        String traceTime = reqParam.get("traceTime");//	交易传输时间
        String settleCurrencyCode = reqParam.get("settleCurrencyCode");//清算币种
        String settleAmt = reqParam.get("settleAmt");//清算金额
        String settleDate = reqParam.get("settleDate");//清算日期
        String traceNo = reqParam.get("traceNo");//系统跟踪号

        //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库
        if ("00".equals(respCode) || "A6".equals(respCode)) {

        }

        //返回给银联200或302状态码，银联判定为通知成功，其他返回码为通知失败
        resp.setStatus(HttpServletResponse.SC_OK);
        return "ok";
    }

    /**
     * 退货
     * 后台资金类交易，有同步应答和后台通知应答
     *
     * 交易说明：
     * 1）以后台通知或交易状态查询交易（Form_6_5_Query）确定交易成功，建议发起查询交易的机制：可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）
     * 2）退货金额不超过总金额，可以进行多次退货
     * 3）退货能对11个月内的消费做（包括当清算日），支持部分退货或全额退货，到账时间较长，一般1-10个清算日（多数发卡行5天内，但工行可能会10天），所有银行都支持
     */
    @RequestMapping("form_6_4_Refund")
    public void form_6_4_Refund(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.config;

        String origQryId = req.getParameter("origQryId");
        String txnAmt = req.getParameter("txnAmt");
        String merId = req.getParameter("merId");

        Map<String, String> data = new HashMap<>();

        // 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        data.put("version", config.getVersion());            //版本号
        data.put("encoding", config.getEncoding());          //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("bizType", "000201");                       //业务类型 B2C网关支付，手机wap支付
        data.put("txnType", "04");                           //交易类型 04-退货
        data.put("txnSubType", "00");                        //交易子类型  默认00
        data.put("signMethod", config.getSignMethod());      //签名方法
        data.put("channelType", "07");                       //渠道类型，07-PC，08-手机

        // 商户接入参数
        data.put("merId", merId);                            //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("orderId", DemoBase.getOrderId());          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", DemoBase.getCurrentTime());      //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("txnAmt", txnAmt);                          //退货金额，单位分，不要带小数点。退货金额小于等于原消费金额，当小于的时候可以多次退货至退货累计金额等于原消费金额
        data.put("accessType", "0");                         //接入类型，商户接入固定填0，不需修改
        data.put("origQryId", origQryId);                    //原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取
        data.put("currencyCode", "156");                     //交易币种（境内商户一般是156 人民币）
        //data.put("reqReserved", "透传信息");                //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        data.put("backUrl", config.getBackUrl());            //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 退货交易 商户通知,其他说明同消费交易的后台通知

        // 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
        //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        Map<String, String> reqData  = AcpService.sign(data, config.getEncoding());

        //这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        Map<String, String> rspData = AcpService.post(reqData, config.getBackTransUrl(), config.getEncoding());

        //对应答码的处理，请根据您的业务逻辑来编写程序，以下应答码处理逻辑仅供参考
        if (rspData.isEmpty()) {
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        } else {
            if (!AcpService.validate(rspData, config.getEncoding())) {
                LogUtil.writeErrorLog("验证签名失败");
            } else {
                String respCode = reqData.get("respCode");//应答码
                String respMsg = reqData.get("respMsg");//应答信息
                String merId2 = reqData.get("merId");//商户代码
                String orderId = reqData.get("orderId");//商户订单号
                String qrCode = reqData.get("qrCode");//二维码数据
                String signMethod = reqData.get("signMethod");//签名方法
                String txnTime = reqData.get("txnTime");//订单发送时间
                String txnType = reqData.get("txnType");//交易类型
                String txnSubType = reqData.get("txnSubType");//交易子类型
                String reqReserved = reqData.get("reqReserved");//请求方保留域
                String txnAmt2 = reqData.get("txnAmt");//交易金额

                if ("00".equals(respCode)) {
                    //交易已受理，等待接收后台通知更新订单状态,也可以主动发起 查询交易确定交易状态。
                } else if("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {
                    //后续需发起交易状态查询交易确定交易状态
                } else {
                    //其他应答码为失败请排查原因
                }
            }
        }

        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);

        resp.getWriter().write("</br>请求报文:<br/>" + reqMessage + "<br/>" + "应答报文:</br>" + rspMessage);
    }

    /**
     * 退货，后台通知
     */
    @RequestMapping("form_6_4_Refund_notify")
    @ResponseBody
    public String form_6_4_Refund_notify(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String encoding = req.getParameter(SDKConstants.param_encoding);

        Map<String, String> reqParam = getAllRequestParam(req);

        if (!AcpService.validate(reqParam, encoding)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "error";
        }

        String respCode = reqParam.get("respCode");//应答码
        String respMsg = reqParam.get("respMsg");//应答信息

        String merId = reqParam.get("merId");//商户代码
        String orderId = reqParam.get("orderId");//商户订单号
        String origQryId = reqParam.get("origQryId");//原交易查询流水号
        String queryId = reqParam.get("queryId");//查询流水号
        String signMethod = reqParam.get("signMethod");//签名方法
        String traceTime = reqParam.get("traceTime");//交易传输时间
        String settleCurrencyCode = reqParam.get("settleCurrencyCode");//清算币种
        String settleAmt = reqParam.get("settleAmt");//清算金额
        String settleDate = reqParam.get("settleDate");//清算日期
        String traceNo = reqParam.get("traceNo");//系统跟踪号

        String txnTime = reqParam.get("txnTime");//订单发送时间
        String txnAmt = reqParam.get("txnAmt");//交易金额
        String txnType = reqParam.get("txnType");//交易类型
        String txnSubType = reqParam.get("txnSubType");//交易子类型
        String reqReserved = reqParam.get("reqReserved");//请求方保留域

        //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库
        if ("00".equals(respCode) || "A6".equals(respCode)) {

        }

        //返回给银联200或302状态码，银联判定为通知成功，其他返回码为通知失败
        resp.setStatus(HttpServletResponse.SC_OK);
        return "ok";
    }

    /**
     * 文件传输类接口
     * 后台获取对账文件交易，只有同步应答
     *
     * 交易说明：
     * 对账文件的格式请参考《全渠道平台接入接口规范 第3部分 文件接口》
     * 对账文件示例见目录file下的802310048993424_20150905.zip
     * 解析落地后的对账文件可以参考BaseDemo.java中的parseZMFile();parseZMEFile();方法
     */
    @RequestMapping("form_6_6_FileTransfer")
    @ResponseBody
    public void form_6_6_FileTransfer(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.config;

        String merId = req.getParameter("merId");
        String settleDate = req.getParameter("settleDate");

        Map<String, String> data = new HashMap<>();

        // 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
        data.put("version", config.getVersion());               //版本号 全渠道默认值
        data.put("encoding", config.getEncoding());             //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("bizType", "000000");                       //业务类型，固定
        data.put("txnType", "76");                           //交易类型 76-对账文件下载
        data.put("txnSubType", "01");                        //交易子类型 01-对账文件下载
        data.put("signMethod", config.getSignMethod());      //签名方法

        // 商户接入参数
        data.put("txnTime",DemoBase.getCurrentTime());       //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("accessType", "0");                         //接入类型，商户接入填0，不需修改
        data.put("merId", merId);                	         //商户代码，请替换正式商户号测试，如使用的是自助化平台注册的777开头的商户号，该商户号没有权限测文件下载接口的，请使用测试参数里写的文件下载的商户号和日期测。如需777商户号的真实交易的对账文件，请使用自助化平台下载文件。
        data.put("settleDate", settleDate);                  //清算日期，如果使用正式商户号测试则要修改成自己想要获取对账文件的日期， 测试环境如果使用700000000000001商户号则固定填写0119
        data.put("fileType", "00");                          //文件类型，一般商户填写00即可

        // 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
        // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        Map<String, String> reqData = AcpService.sign(data, config.getEncoding());

        Map<String, String> rspData = AcpService.post(reqData, config.getFileTransUrl(), config.getEncoding());

        String fileContentDispaly = "";
        if (rspData.isEmpty()) {
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        } else {
            if (!AcpService.validate(rspData, config.getEncoding())) {
                LogUtil.writeErrorLog("验证签名失败");
            } else {

                String respCode = rspData.get("respCode");//应答码
                String respMsg = rspData.get("respMsg");//应答信息

                String merId2 = rspData.get("merId");//商户代码
                String settleDate2 = rspData.get("settleDate");//清算日期

                String fileType = rspData.get("fileType");//文件类型
                String fileContent = rspData.get("fileContent");//批量文件内容
                String fileName = rspData.get("fileName");//文件名
                String txnTime = rspData.get("txnTime");//订单发送时间
                String txnType = rspData.get("txnType");//交易类型
                String txnSubType = rspData.get("txnSubType");//交易子类型

                if ("00".equals(respCode)) {
                    String outPutDirectory ="d:/";

                    // 交易成功，解析返回报文中的fileContent并落地
                    String zipFilePath = AcpService.deCodeFileContent(rspData, outPutDirectory, config.getEncoding());

                    //对落地的zip文件解压缩并解析
                    List<String> fileList = DemoBase.unzip(zipFilePath, outPutDirectory);

                    //解析ZM，ZME文件
                    fileContentDispaly = "<br>获取到商户对账文件，并落地到" + outPutDirectory + ",并解压缩 <br>";

                    for (String file : fileList) {
                        if (file.indexOf("ZM_") != -1) {
                            List<Map> ZmDataList = DemoBase.parseZMFile(file);
                            fileContentDispaly += DemoBase.getFileContentTable(ZmDataList, file);
                        } else if(file.indexOf("ZME_") != -1) {
                            DemoBase.parseZMEFile(file);
                        }
                    }
                } else {
                    //其他应答码为失败请排查原因
                }
            }
        }

        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);

        resp.getWriter().write("</br>请求报文:<br/>" + reqMessage + "<br/>" + "应答报文:</br>" + rspMessage + fileContentDispaly);
    }

}
