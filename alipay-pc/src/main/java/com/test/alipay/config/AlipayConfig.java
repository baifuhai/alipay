package com.test.alipay.config;

import java.io.FileWriter;
import java.io.IOException;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *修改日期：2017-04-05
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */
public class AlipayConfig {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016092500590658";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAEi2xNJ/oHRQBLzlJDNuVOQL2h3HUsfQWHOJKSdYg5lhegK4gYxeJvVWT3rZK7bwm27SI77zMKAUzBC9PmcXQ0fPOw7b6g+ee84HwZVIG3EU6SFDExxfd5oY3ZTWgNiAp9TSIt+cEDtYlxy3qPhmSGqT0DIlo0/3OflgTneaPewNBw90RUFwgp+yloNRiXVs6lTlf98lCtoVFc8glb8Hy8tqr8LCBqbDhuOFm8csu/eRsoY3+zPS6zw9tPBobwqI8lv+I8aAVvr9KqzmJwaImb/H192ZE0ZOciTuRqvegzl5dYxSY2n2tskm801GMUj++ecL700RxPYfv1+KCT6UxAgMBAAECggEAKDZmHJiw9e7IlmrlqnanrnlONoErAuXy/YI0mmsVCrRPQhHc4uj8L5lVRm01a0CUyOnsxVK0C2ZBmGnC4f6G3O5oBh0RvSdKogvHY6ZC4y7Qy6ACUQCB7bJq3UQyUwWh/EbbJdii5EWa7jPiWA2CWwV7DaFmT2060TXAiPLhJ56zkvgy/Gq+rhUm5/8VYFrEeQ3ygoh4sFBooepcA+oRwDkDSzV2Cke3sLM/ZTJSGXLGONgP1V3SdnRK3UrQCglAyuI0oiyCQs48ndrl/7GMZ+DxK2UP6uhNsU6ysOVetGwt2YDTG2ZJ1vunub7ygsA81PznTZAC3VFcO339JUqVxQKBgQDqpXFEHgTAEFgE/BfCesVVR4De+YfLCI1UsXR/W2TrIccG0PxG4ewPwcAUybKyXAxloHys8qL9XFK8wSS/uSxgaiKWIhZkJM9+ab7dHOSGaYS0RAxTPaZ+f3jsuYECFRoLXhGvHWKmb675dy9jXHsLcTdXmLSXyoMSk2S8BXTRjwKBgQDRjNwas7crIgVxY66LxHpJTnmUD8YJl/gaUEoHF0hvE2QlH5TrL8zRMpMYZJH33EZDOyatHZizsWxFgfy4+0sblxrrqV4qItDHHiqWLYxppNNW/QKwXhbwlt8W5eULsjJxrR5zMP+gN1MGI19guyk/zy1BpD9V9lX6NJyh/s89PwKBgQCuq+2/OWFr4D7FpyvAuEfBjfLfCX4OpBUhYOBKFizv5DsWVegWlAN4T1C/qM3/fAo2dNuamxy94kYtANJsblocg6WvgTyN2+EiR3Gvg9ySOmZxEt7h8FjKffX0srrYZAD5SVN8ujE/mI/2YMTEAIoQIH2EFccJ39TmtRYd6Snm/QKBgD4dBLkdgDPW18oug/SF/sFv83XB3y/EzhPurKLBcgUKuGqQm/HCr3FrDlLyrJnNvz36cJJr7XGGK9PGngSu6Cp7dc1Y3MKco0oCVRlC0xYVd1eXk453tVEHR4VgC66V2YH8kMQQSBVTkNaG8aSYlbeGT6Hfb66pX/7UTYFvKDq9AoGBAMaHOT+w16KhL659YK5aTIK0kPU2saPqqwdB39TymEFiclkOL3US++RRepRShrpWv0xNpQSXQwXO1M8YjduSDDUhimoI2PEtFGE5LEOLqg5GDqD4/MBy/w3tP8zXQgxJ+jCPcOGs0/PTnoCIFsdUQvxMQJRQXXV5ojb5rQOQglxG";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA03Xs2afdnS250sveqU51quDa4bO6WjnfaQ62LXHqRvtyZUTM+HE30NsT+a46RlQCbx62Q8lF9h75l/4Oq5t2wciC9pRH4tkj8ZtWTCLOFtvbnNUve6MQzaUP2EqA8xk4AWpTLMAgDdN5RBSMsKyiS+Hr67bES4TMxP+AR+DMs3a0RvWdAMmDFIJJ6h5D2tni5pll9v+wqfokDd4Wgbi4gFAM2g0L4tItxDetTrjPYv8KgwcCfgqpwlW3wU0PQBAhv2zwrVGuMVbrJF9KP2FlMAvdgOWYL9FC8oZqfQLzHp213aXb+H6F/x6ZfSAFLlhoY6yPgP/sP3paD1+5R+hymQIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8080/alipay/notifyUrl";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8080/alipay/returnUrl";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 数据格式
    public static String format = "json";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:/";

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis() + ".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

