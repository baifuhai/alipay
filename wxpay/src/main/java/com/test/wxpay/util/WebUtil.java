package com.test.wxpay.util;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {

    /**
     * 输出html
     *
     * @param result
     * @param response
     * @throws Exception
     */
    public static void writeHtml(String result, HttpServletResponse response) throws Exception {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    /**
     * 输出json
     *
     * @param result
     * @param response
     * @throws Exception
     */
    public static void writeJson(String result, HttpServletResponse response) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.flush();
        out.close();
    }

    /**
     * 下载
     *
     * @param sUrl
     * @param response
     * @throws Exception
     */
    public static void download(String sUrl, HttpServletResponse response) throws Exception {
        URL url = null;
        HttpURLConnection httpUrlConnection = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            url = new URL(sUrl);
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setConnectTimeout(5 * 1000);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestMethod("GET");
            httpUrlConnection.connect();
            is = httpUrlConnection.getInputStream();

            os = response.getOutputStream();

            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (httpUrlConnection != null) {
                httpUrlConnection.disconnect();
            }
        }
    }

    public static void urlToImage(String codeUrl, OutputStream outputStream) {
    }

}
