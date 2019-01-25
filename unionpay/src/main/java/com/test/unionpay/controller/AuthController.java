package com.test.unionpay.controller;

import com.test.unionpay.util.DemoUtil;
import com.unionpay.acp.sdk.AcpService;
import com.unionpay.acp.sdk.LogUtil;
import com.unionpay.acp.sdk.SDKConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@RequestMapping("")
@Controller
public class AuthController {

    /**
     * 预授权
     * 前台跳转，有前台通知应答和后台通知应答

     * 交易说明:
     * 1）以后台通知或交易状态查询交易确定交易成功,前台通知不能作为判断成功的标准.
     * 2）交易状态查询交易（Form_6_5_Query）建议调用机制：前台类交易间隔（5分、10分、30分、60分、120分）发起交易查询，如果查询到结果成功，则不用再查询。
     * （失败，处理中，查询不到订单均可能为中间状态）。也可以建议商户使用payTimeout（支付超时时间），过了这个时间点查询，得到的结果为最终结果。
     */
    @RequestMapping("form_6_7_1_AuthDeal_Front")
    public void form_6_7_1_AuthDeal_Front(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.getInstance();

        /**
         * 请求银联接入地址，获取证书文件，证书路径等相关参数初始化到SDKConfig类中
         * 在java main 方式运行时必须每次都执行加载
         * 如果是在web应用开发里,这个方法可使用监听的方式写入缓存,无须在这出现
         */
        //这里已经将加载属性文件的方法挪到了web/AutoLoadServlet.java中
        //config.loadPropertiesFromSrc(); //从classpath加载acp_sdk.properties文件

        //前台页面传过来的
        String merId = req.getParameter("merId");
        String txnAmt = req.getParameter("txnAmt");

        Map<String, String> requestData = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        requestData.put("version", config.getVersion());   			  //版本号，全渠道默认值
        requestData.put("encoding", config.getEncoding()); 			  //字符集编码，可以使用UTF-8,GBK两种方式
        requestData.put("signMethod", config.getSignMethod()); //签名方法
        requestData.put("txnType", "02");               			  //交易类型 ，02：预授权
        requestData.put("txnSubType", "01");            			  //交易子类型， 01：预授权
        requestData.put("bizType", "000201");           			  //业务类型，B2C网关支付，手机wap支付
        requestData.put("channelType", "07");           			  //渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机

        /***商户接入参数***/
        requestData.put("merId", merId);    	          			  //商户号码，请改成自己申请的正式商户号或者open上注册得来的777测试商户号
        requestData.put("accessType", "0");             			  //接入类型，0：直连商户
        requestData.put("orderId", DemoUtil.getOrderId());             //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
        requestData.put("txnTime", DemoUtil.getCurrentTime());        //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        requestData.put("currencyCode", "156");         			  //交易币种（境内商户一般是156 人民币）
        requestData.put("txnAmt", txnAmt);             			      //交易金额，单位分，不要带小数点
        //requestData.put("reqReserved", "透传字段");        		      //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。

        //前台通知地址 （需设置为外网能访问 http https均可），支付成功后的页面 点击“返回商户”按钮的时候将异步通知报文post到该地址
        //如果想要实现过几秒中自动跳转回商户页面权限，需联系银联业务（发邮件到operation@unionpay.com）申请开通自动返回商户权限
        //异步通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        requestData.put("frontUrl", config.getFrontUrl());

        //后台通知地址（需设置为【外网】能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，失败的交易银联不会发送后台通知
        //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
        //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
        //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
        requestData.put("backUrl", config.getBackUrl());

        // 订单超时时间。
        // 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。 跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户。
        // 此时间建议取支付时的北京时间加15分钟。
        // 超过超时时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败。
        requestData.put("payTimeout", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime() + 15 * 60 * 1000));
        requestData.put("riskRateInfo", "{commodityName=测试商品名称}");

        //////////////////////////////////////////////////
        //
        //       报文中特殊用法请查看 PC wap网关跳转预授权特殊用法.txt
        //
        //////////////////////////////////////////////////

        /**请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面**/
        Map<String, String> submitFromData = AcpService.sign(requestData, config.getEncoding());  //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String requestFrontUrl = config.getFrontTransUrl();  //获取请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.frontTransUrl
        String html = AcpService.createAutoFormHtml(requestFrontUrl, submitFromData, config.getEncoding());   //生成自动跳转的Html表单

        LogUtil.writeLog("打印请求HTML，此为请求报文，为联调排查问题的依据："+html);
        //将生成的html写到浏览器中完成自动跳转打开银联支付页面；这里调用signData之后，将html写到浏览器跳转到银联页面之前均不能对html中的表单项的名称和值进行修改，如果修改会导致验签不通过
        resp.getWriter().write(html);
    }


    /**
     * 预授权撤销
     * 后台交易，有同步应答和后台通知应答<br>

     * 交易说明:
     * 1）以后台通知或交易状态查询交易（Form_6_5_Query）确定交易成功。建议发起查询交易的机制：可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）
     * 2）预授权撤销对清算日30天之内（包括第30天）的预授权做，必须为预授权金额全额撤销。
     */
    @RequestMapping("form_6_7_2_AuthUndo")
    public void form_6_7_2_AuthUndo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.getInstance();

        String origQryId = req.getParameter("origQryId");
        String txnAmt = req.getParameter("txnAmt");
        String merId = req.getParameter("merId");

        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", config.getVersion());            //版本号
        data.put("encoding", config.getEncoding());          //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", config.getSignMethod()); //签名方法
        data.put("txnType", "32");                        //交易类型 31-预授权撤销
        data.put("txnSubType", "00");                     //交易子类型  默认00
        data.put("bizType", "000201");                    //业务类型 B2C网关支付，手机wap支付
        data.put("channelType", "07");                    //渠道类型，07-PC，08-手机

        /***商户接入参数***/
        data.put("merId", merId);             //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                      //接入类型，商户接入固定填0，不需修改
        data.put("orderId", DemoUtil.getOrderId());       //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", DemoUtil.getCurrentTime());   //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("txnAmt", txnAmt);                       //【撤销金额】，撤销时必须和原消费金额相同
        data.put("currencyCode", "156");                  //交易币种(境内商户一般是156 人民币)
        //data.put("reqReserved", "透传信息");                 //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        data.put("backUrl", config.getBackUrl());            //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费撤销交易 商户通知,其他说明同消费交易的商户通知

        /***要调通交易以下字段必须修改***/
        data.put("origQryId", origQryId);   			  //【原始交易流水号】，原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取


        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文**/

        Map<String, String> reqData  = AcpService.sign(data, config.getEncoding());//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = config.getBackTransUrl();//交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

        Map<String,String> rspData = AcpService.post(reqData, url, config.getEncoding());//发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, config.getEncoding())){
                LogUtil.writeLog("验证签名成功");
                String respCode = rspData.get("respCode");
                if("00".equals(respCode)){
                    //交易已受理(不代表交易已成功），等待接收后台通知确定交易成功，也可以主动发起 查询交易确定交易状态。
                    //TODO
                }else if("03".equals(respCode) ||
                        "04".equals(respCode) ||
                        "05".equals(respCode)){
                    //后续需发起交易状态查询交易确定交易状态。
                    //TODO
                }else{
                    //其他应答码为失败请排查原因
                    //TODO
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        }
        String reqMessage = DemoUtil.genHtmlResult(reqData);
        String rspMessage = DemoUtil.genHtmlResult(rspData);
        resp.getWriter().write("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+"");
    }

    /**
     * 预授权完成
     * 后台交易，有同步应答和后台通知应答<br>

     * 交易说明:
     * 1）以后台通知或交易状态查询交易（Form_6_5_Query）确定交易成功。建议发起查询交易的机制：可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）
     * 2）预授权完成交易必须在预授权交易30日内发起，否则预授权交易自动解冻。预授权完成金额可以是预授权金额的(0-115%] （大于0小于等于115）
     */
    @RequestMapping("form_6_7_3_AuthFinish")
    public void form_6_7_3_AuthFinish(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SDKConfig config = SDKConfig.getInstance();

        String origQryId = req.getParameter("origQryId");
        String txnAmt = req.getParameter("txnAmt");
        String merId = req.getParameter("merId");

        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", config.getVersion());            //版本号
        data.put("encoding", config.getEncoding());          //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", config.getSignMethod()); //签名方法
        data.put("txnType", "03");                        //交易类型 03-预授权完成
        data.put("txnSubType", "00");                     //交易子类型  默认00
        data.put("bizType", "000201");                    //业务类型 B2C网关支付，手机wap支付
        data.put("channelType", "07");                    //渠道类型，07-PC，08-手机

        /***商户接入参数***/
        data.put("merId", merId);             //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                      //接入类型，商户接入固定填0，不需修改
        data.put("orderId", DemoUtil.getOrderId());       //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", DemoUtil.getCurrentTime());   //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("txnAmt", txnAmt);                       //【完成金额】，金额范围为预授权金额的0-115%
        data.put("currencyCode", "156");                  //交易币种(境内商户一般是156 人民币)
        //data.put("reqReserved", "透传信息");                 //请求方保留域，如需使用请启用即可；透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        data.put("backUrl", config.getBackUrl());            //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费撤销交易 商户通知,其他说明同消费交易的商户通知


        /***要调通交易以下字段必须修改***/
        data.put("origQryId", origQryId);   			  //【原始交易流水号】，原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文**/

        Map<String, String> reqData  = AcpService.sign(data, config.getEncoding());//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
        String url = config.getBackTransUrl();//交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl

        Map<String,String> rspData = AcpService.post(reqData, url, config.getEncoding());//发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, config.getEncoding())){
                LogUtil.writeLog("验证签名成功");
                String respCode = rspData.get("respCode");
                if("00".equals(respCode)){
                    //交易已受理(不代表交易已成功），等待接收后台通知确定交易成功，也可以主动发起 查询交易确定交易状态。
                    //TODO
                }else if("03".equals(respCode) ||
                        "04".equals(respCode) ||
                        "05".equals(respCode)){
                    //后续需发起交易状态查询交易确定交易状态。
                    //TODO
                }else{
                    //其他应答码为失败请排查原因
                    //TODO
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        }
        String reqMessage = DemoUtil.genHtmlResult(reqData);
        String rspMessage = DemoUtil.genHtmlResult(rspData);
        resp.getWriter().write("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+"");
    }


    /**
     * 预授权完成撤销
     * 后台交易，有同步应答和后台通知应答<br>

     * 交易说明:
     * 1）以后台通知或交易状态查询交易（Form_6_5_Query）确定交易成功。建议发起查询交易的机制：可查询N次（不超过6次），每次时间间隔2N秒发起,即间隔1，2，4，8，16，32S查询（查询到03，04，05继续查询，否则终止查询）
     * 2）预授权完成撤销交易仅能对当清算日的预授权做，必须为预授权完成金额全额撤销。
     */
    @RequestMapping("form_6_7_4_AuthFinishUndo")
    public void form_6_7_4_AuthFinishUndo(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        SDKConfig config = SDKConfig.getInstance();

        String origQryId = req.getParameter("origQryId");
        String txnAmt = req.getParameter("txnAmt");
        String merId = req.getParameter("merId");

        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", config.getVersion());            //版本号
        data.put("encoding", config.getEncoding());          //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", config.getSignMethod()); //签名方法
        data.put("txnType", "33");                        //交易类型 03-预授权完成
        data.put("txnSubType", "00");                     //交易子类型  默认00
        data.put("bizType", "000201");                    //业务类型 B2C网关支付，手机wap支付
        data.put("channelType", "07");                    //渠道类型，07-PC，08-手机

        /***商户接入参数***/
        data.put("merId", merId);             //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        data.put("accessType", "0");                      //接入类型，商户接入固定填0，不需修改
        data.put("orderId", DemoUtil.getOrderId());       //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
        data.put("txnTime", DemoUtil.getCurrentTime());   //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("txnAmt", txnAmt);                       //【完成金额】，金额范围为预授权金额的0-115%
        data.put("currencyCode", "156");                  //交易币种(境内商户一般是156 人民币)
        //data.put("reqReserved", "透传信息");                 //如需使用请启用即可；请求方保留域，透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
        data.put("backUrl", config.getBackUrl());            //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费撤销交易 商户通知,其他说明同消费交易的商户通知


        /***要调通交易以下字段必须修改***/
        data.put("origQryId", origQryId);   			  //【原始交易流水号】，原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取


        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文**/

        Map<String, String> reqData  = AcpService.sign(data, config.getEncoding());//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        String url = config.getBackTransUrl();//交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        Map<String,String> rspData =  AcpService.post(reqData, url, config.getEncoding());//发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, config.getEncoding())){
                LogUtil.writeLog("验证签名成功");
                String respCode = rspData.get("respCode");
                if("00".equals(respCode)){
                    //交易已受理(不代表交易已成功），等待接收后台通知确定交易成功，也可以主动发起 查询交易确定交易状态。
                    //TODO
                }else if("03".equals(respCode) ||
                        "04".equals(respCode) ||
                        "05".equals(respCode)){
                    //后续需发起交易状态查询交易确定交易状态。
                    //TODO
                }else{
                    //其他应答码为失败请排查原因
                    //TODO
                }
            }else{
                LogUtil.writeErrorLog("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        }else{
            //未返回正确的http状态
            LogUtil.writeErrorLog("未获取到返回报文或返回http状态码非200");
        }
        String reqMessage = DemoUtil.genHtmlResult(reqData);
        String rspMessage = DemoUtil.genHtmlResult(rspData);
        resp.getWriter().write("</br>请求报文:<br/>"+reqMessage+"<br/>" + "应答报文:</br>"+rspMessage+"");
    }

}
