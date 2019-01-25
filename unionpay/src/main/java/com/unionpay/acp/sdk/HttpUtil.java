package com.unionpay.acp.sdk;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {

	private static final String REQUEST_METHOD_GET = "GET";
	private static final String REQUEST_METHOD_POST = "POST";

	/**
	 * get请求
	 *
	 * @param urlStr
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static Result sendGet(String urlStr, String encoding) throws Exception {
		return request(urlStr, null, REQUEST_METHOD_GET, encoding);
	}

	/**
	 * post请求
	 *
	 * @param urlStr
	 * @param data
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static Result sendPost(String urlStr, Map<String, String> data, String encoding) throws Exception {
		return request(urlStr, getRequestParamString(data, encoding), REQUEST_METHOD_POST, encoding);
	}

	/**
	 * 请求
	 *
	 * @param urlStr
	 * @param data
	 * @param requestMethod
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	private static Result request(String urlStr, String data, String requestMethod, String encoding) throws Exception {
		SDKConfig config = SDKConfig.getInstance();

		PrintStream out = null;
		InputStream in = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(30000);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encoding);
			connection.setRequestMethod(requestMethod);

			if (REQUEST_METHOD_POST.equals(requestMethod)) {
				connection.setDoInput(true);
				connection.setDoOutput(true);
			}

			if ("https".equalsIgnoreCase(url.getProtocol())) {
				HttpsURLConnection husn = (HttpsURLConnection) connection;
				//是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
				if (!config.isIfValidateRemoteCert()) {
					husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
					husn.setHostnameVerifier(new TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情况
				}
			}

			connection.connect();

			if (REQUEST_METHOD_POST.equals(requestMethod) && StringUtils.isNotBlank(data)) {
				out = new PrintStream(connection.getOutputStream(), false, encoding);
				out.print(data);
				out.flush();
			}

			int code = connection.getResponseCode();
			if (code == HttpServletResponse.SC_OK) {
				in = connection.getInputStream();
			} else {
				in = connection.getErrorStream();
			}

			String s = new String(IOUtils.toByteArray(in), encoding);

			return new Result(code, s);
		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * 将map转换为key=value&key=value
	 *
	 * @param requestParam
	 * @param encoding
	 * @return
	 */
	private static String getRequestParamString(Map<String, String> requestParam, String encoding) throws Exception {
		if (requestParam != null && requestParam.size() != 0) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> en : requestParam.entrySet()) {
				sb.append("&");
				sb.append(en.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode(en.getValue(), encoding));
			}
			return sb.substring(1);
		}
		return null;
	}

	public static class Result {

		private int code;
		private String response;

		public Result(int code, String response) {
			this.code = code;
			this.response = response;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getResponse() {
			return response;
		}

		public void setResponse(String response) {
			this.response = response;
		}

	}

	/**
	 * 忽略验证ssl证书
	 */
	private static class BaseHttpSSLSocketFactory extends SSLSocketFactory {

		@Override
		public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {
			return getSSLContext().getSocketFactory().createSocket(arg0, arg1, arg2, arg3);
		}

		@Override
		public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
			return getSSLContext().getSocketFactory().createSocket(arg0, arg1);
		}

		@Override
		public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
			return getSSLContext().getSocketFactory().createSocket(arg0, arg1, arg2, arg3);
		}

		@Override
		public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
			return getSSLContext().getSocketFactory().createSocket(arg0, arg1);
		}

		@Override
		public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
			return getSSLContext().getSocketFactory().createSocket(arg0, arg1, arg2, arg3);
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return null;
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return null;
		}

		private SSLContext getSSLContext() {
			return createEasySSLContext();
		}

		private SSLContext createEasySSLContext() {
			try {
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(null, new TrustManager[] { new MyX509TrustManager() }, null);
				return context;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private static class MyX509TrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}
	}

	/**
	 * 解决由于服务器证书问题导致HTTPS无法访问的情况 PS:HTTPS hostname wrong: should be <localhost>
	 */
	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			//直接返回true
			return true;
		}
	}

}
