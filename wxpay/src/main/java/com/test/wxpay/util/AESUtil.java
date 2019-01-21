package com.test.wxpay.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class AESUtil {

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加解密算法/工作模式/填充方式
     * Java本身限制密钥的长度最多128位，而AES256需要的密钥长度是256位，因此需要到Java官网上下载一个Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
     * 下载后打开压缩包，里面有两个jar文件，把这两个jar文件解压到JRE目录下的lib/security文件夹，覆盖原来的文件，这样就没有密钥长度的限制了
     */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";

    /**
     * 商户key
     */
    private static final String MCH_KEY = "2IBtBXdrqC3kCBs4gaceL7nl2nnFadQv";

    /**
     * key
     */
    private static SecretKeySpec KEY;

    static {
        //BouncyCastle，一个开源加/解密类库中的加/解密算法提供者类
        //Android的Java运行环境中包含了"AES/ECB/PKCS7Padding"算法，但一般的JRE（如Oracle JRE、OpenJRE）里面只有"AES/ECB/PKCS5Padding"算法，没有"AES/ECB/PKCS7Padding"算法
        //故我们需要引入BouncyCastle的库，并给Cipher.getInstance方法传入参数"BC"来指定Java使用这个库里的加/解密算法
        Security.addProvider(new BouncyCastleProvider());

        try {
            KEY = new SecretKeySpec(MD5Util.encode(MCH_KEY).toLowerCase().getBytes("UTF-8"), ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AES加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
        cipher.init(Cipher.ENCRYPT_MODE, KEY);
        return Base64Util.encode(cipher.doFinal(data.getBytes("UTF-8")));
    }

    /**
     * AES解密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
        cipher.init(Cipher.DECRYPT_MODE, KEY);
        return new String(cipher.doFinal(Base64Util.decode(data)));
    }

    public static void main(String[] args) throws Exception {
        //加密
        String s = "<root>"+
                "<out_refund_no><![CDATA[2531340110812300]]></out_refund_no>"+
                "<out_trade_no><![CDATA[2531340110812100]]></out_trade_no>"+
                "<refund_account><![CDATA[REFUND_SOURCE_RECHARGE_FUNDS]]></refund_account>"+
                "<refund_fee><![CDATA[1]]></refund_fee>"+
                "<refund_id><![CDATA[50000505542018011003064518841]]></refund_id>"+
                "<refund_recv_accout><![CDATA[支付用户零钱]]></refund_recv_accout>"+
                "<refund_request_source><![CDATA[API]]></refund_request_source>"+
                "<refund_status><![CDATA[SUCCESS]]></refund_status>"+
                "<settlement_refund_fee><![CDATA[1]]></settlement_refund_fee>"+
                "<settlement_total_fee><![CDATA[1]]></settlement_total_fee>"+
                "<success_time><![CDATA[2018-01-10 10:31:24]]></success_time>"+
                "<total_fee><![CDATA[1]]></total_fee>"+
                "<transaction_id><![CDATA[4200000052201801101409025381]]></transaction_id>"+
                "</root>";
        s = "<root><out_refund_no><![CDATA[2531340110812300]]></out_refund_no><out_trade_no><![CDATA[2531340110812100]]></out_trade_no><refund_account><![CDATA[REFUND_SOURCE_RECHARGE_FUNDS]]></refund_account><refund_fee><![CDATA[1]]></refund_fee><refund_id><![CDATA[50000505542018011003064518841]]></refund_id><refund_recv_accout><![CDATA[支付用户零钱]]></refund_recv_accout><refund_request_source><![CDATA[API]]></refund_request_source><refund_status><![CDATA[SUCCESS]]></refund_status><settlement_refund_fee><![CDATA[1]]></settlement_refund_fee><settlement_total_fee><![CDATA[1]]></settlement_total_fee><success_time><![CDATA[2018-01-10 10:31:24]]></success_time><total_fee><![CDATA[1]]></total_fee><transaction_id><![CDATA[4200000052201801101409025381]]></transaction_id></root>";
        String reqInfo = AESUtil.encrypt(s);
        System.out.println(reqInfo);

        //解密
        //reqInfo = "Ih5osM/5IbPfHouVrUmwebd1yAW2Gys91jv006W1237sSi3z022KxHafLIDMrQLYiBttTadgvy2cbx6DnmwDIQ52lPWfo6pAAHt7Q9DjBIpDRQ7JsbEBlomoQP2ZkdNHnWscVYuFEVlItaSlkSlcKLdB4UwMduqDYseFsUUthz6htPeBu987zXS6dKrgIbRwOxt5RfPmk1sf0oVB2yU3UH0Ly8SzBjmN1jrh4qAaUkfH6VkeMJcsZSGchQn2VresxJTbGH++JE1UsXUF3gyYpweyxBPtHoKdaggsIONR20UKNxJYPJLnEOnfQF/Ipmk8/QmTVRK7iqfVLC9EA1Auma0AlKBjZlYqynUlF3y+E2ZzgWMUlvDHZVWDbzp/TcE0q+Ukc7yQ3HBsibDR474SPlLTkCWz1iydXzkVcLqJKamsh76Liv1a0hzu0sI3qasMAfmwU6/q7/N6quq031toO1GxqkVaxBRK7e64gSOx9ArxxVFgZ7WN+JPq2OH/pTKH8ToxHA0rtxN5+aAgZGkXiIOUiHtp4mjpRxqe34WK7C7Nr0DQyOVwsXT2TTegSgWGm34aa//ZYxHedubv2iX+E7K222lptg9IqHlMXBbwKFtKtIcal61+8ciz+sB1FBpqHchC+3whTqWv5ZANiHBzaOhbIbA/mKX2XZ6Cy0iYh+bL/8Y/Hvz/UnMGzor+2anIUeBAGRQmseL4jY+Qic46WLuEhDcarCaO4JgJSAOC+VmsdrER9TRum26PFwTQwtNpxkrKCiO9Gv36Ood5D8hXnLHUH+4nbsek8ouxkCcFXq4Us0mipB3i5ksQpt23LiJm9Ahxyvptp9Q41SytS48NXiz3IxTOqDdknowedZwAtJ/fhBlwiOHD9N+pECXuNBKLaCZcatGycr0/DPELiCF+MIRQ6V60wzaZD74TKRFULd1ljNsoQIAbuGaT40WMDY6a28jBHQ/IXnD4gvSvfeumwQzp3Q9PiPyFtF6JxH7RBRj9/lmQuQozJIPZCaCNVTBfWQOdcFaBnPLN0ZNvzjA93g6jcIxHzkXHmiGfh98vq2E=";
        //reqInfo = "m4NnwrtY0jhpDgNp65H1V/0OWMtSoTYhhY89MHbflhmnaHq9ZKjx9ABq6Jpg4SccA876HVy7J9P85NpdvCMNGInZ4fANDRE+YfZe4HeF+bbFj6JETcEFPpE9YW+bTbC0D+gl/otScJfvB2QUK7+EeBGPHN1EWX9zbr2Gw6AUaORdFk3mGxV5dtjuwWQrv5juzkXDs33Z2dUMslO+i3j0cTDHqwS4hptx2j6h2HvzgzltFbjo7nysU+4rArqJvrGW/9r18e1St9XgG21NALqixfaSmqetOR4zLVL4/+z3CEz8cg5r+/4GUOTf3XFmLCZ/wEkRQhKRNVibG0NFfiG3KnqArMJ/dheQHCd7qL+XX/ZV6tj8RLMgL7R6hOiR03Ljyikdxq9M3K9CTYgf03pHJd3geXX1LgXrLxc1flL6NW+zD3ZayGYpr1WpLsSMG7z8W5j1pme6cRj3n2+CwSFnOnOkxaFuLKoJAJIqM3gbC0eN++vY73RKphlI4zZqg6o5s3MXI6ju1yoi/ZQ+XbTg2JttsdbU0aySernKwkt0rYMf0j/Mcvo2axgHbI3w/iTm141WxHUjkQ+ga2X1pOWdGifGhSmMP8oGaA/WD5MAsK1qXX0eFvUWS/PTauCSTWq5Cmr8loA/KL3jgvB0nyR4mfccB+tPy4Ny7kzOlr/VNeb0ULf96R0AWFWCtdt8AmujAP0DYiM5FSmYLI0XRhpSDjnEbBM8+isNE1GlAVR3NzzemwQORihScovpAktbRSN/d3N+NgTjSoVDiJvCOxCs3thX9qt9iwYbA+/X/gv8lza2FZyIzwkQxGRcYl8JWKpXzNW8EWUNVnSLdHvQttDeV3CvgP/x579RGd6whyFYS6AaI0qw7oTjCFh2EHS/VzGvFuv166ZlVIJ4MNvg79O9h63ZOSE1LzVqEsVh8fDCfM2GgJ9aUdl95Djgunit4yIZOdoigR3f/BEHKrYCEham11rYohaAXs4XAXWihsV3WD5j4G/P+txvjAwujvf4HDwzHgFsmSml013U2mUiy+v4zw==";
        String s2 = AESUtil.decrypt(reqInfo);
        System.out.println(s2);
    }

}
