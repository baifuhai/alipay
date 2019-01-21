package com.test.wxpay.config;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.*;

public class MyWxpayConfig implements WXPayConfig {

    private byte[] certData;

    public MyWxpayConfig() throws Exception {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("path/to/apiclient_cert.p12");
            baos = new ByteArrayOutputStream();

            byte[] buf = new byte[10 * 1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.flush();

            this.certData = baos.toByteArray();
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        String certPath = "/path/to/apiclient_cert.p12";
//        File file = new File(certPath);
//        InputStream certStream = new FileInputStream(file);
//        certData = new byte[(int) file.length()];
//        certStream.read(certData);
//        certStream.close();
    }

    @Override
    public String getAppID() {
        return null;
    }

    @Override
    public String getMchID() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public InputStream getCertStream() {
        return new ByteArrayInputStream(this.certData);
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 1000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 1000;
    }

}
