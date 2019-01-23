package com.unionpay.acp.sdk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.unionpay.acp.sdk.BaseHttpSSLSocketFactory.TrustAnyHostnameVerifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 发送后台http请求类
 */
public class HttpClient {

	/**
	 * 目标地址
	 */
	private URL url;

	/**
	 * 通信连接超时时间
	 */
	private int connectionTimeout;

	/**
	 * 通信读超时时间
	 */
	private int readTimeout;

	/**
	 * 通信结果
	 */
	private String result;

	/**
	 * 获取通信结果
	 * @return
	 */
	public String getResult() {
		return result;
	}

	/**
	 * 设置通信结果
	 * @param result
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * 构造函数
	 * @param url 目标地址
	 * @param connectionTimeout HTTP连接超时时间
	 * @param readTimeout HTTP读写超时时间
	 */
	public HttpClient(String url, int connectionTimeout, int readTimeout) throws Exception {
		this.url = new URL(url);
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	/**
	 * 发送信息到服务端
	 * @param data
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public int send(Map<String, String> data, String encoding) throws Exception {
		HttpURLConnection httpURLConnection = createPostConnection(encoding);
		String sendData = this.getRequestParamString(data, encoding);
		this.requestServer(httpURLConnection, sendData, encoding);
		this.result = this.response(httpURLConnection, encoding);
		return httpURLConnection.getResponseCode();
	}
	
	/**
	 * 发送信息到服务端 GET方式
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public int sendGet(String encoding) throws Exception {
		HttpURLConnection httpURLConnection = createGetConnection(encoding);
		this.result = this.response(httpURLConnection, encoding);
		return httpURLConnection.getResponseCode();
	}

	/**
	 * HTTP Post发送消息
	 *
	 * @param connection
	 * @param message
	 * @throws IOException
	 */
	private void requestServer(URLConnection connection, String message, String encoding) throws Exception {
		PrintStream out = null;
		try {
			connection.connect();
			out = new PrintStream(connection.getOutputStream(), false, encoding);
			out.print(message);
			out.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 显示Response消息
	 *
	 * @param connection
	 * @return
	 * @throws Exception
	 */
	private String response(final HttpURLConnection connection, String encoding) throws Exception {
		InputStream in = null;
		BufferedReader br = null;
		try {
			if (200 == connection.getResponseCode()) {
				in = connection.getInputStream();
			} else {
				in = connection.getErrorStream();
			}
			LogUtil.writeLog("HTTP Return Status-Code:[" + connection.getResponseCode() + "]");
			return new String(IOUtils.toByteArray(in), encoding);
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != br) {
				br.close();
			}
			if (null != in) {
				in.close();
			}
			if (null != connection) {
				connection.disconnect();
			}
		}
	}
	
	/**
	 * 创建连接post
	 *
	 * @return
	 * @throws Exception
	 */
	private HttpURLConnection createPostConnection(String encoding) throws Exception {
		SDKConfig config = SDKConfig.config;

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(this.connectionTimeout);
		connection.setReadTimeout(this.readTimeout);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=" + encoding);
		connection.setRequestMethod("POST");

		if ("https".equalsIgnoreCase(url.getProtocol())) {
			HttpsURLConnection husn = (HttpsURLConnection) connection;
			//是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
			if (!config.isIfValidateRemoteCert()) {
				husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
				husn.setHostnameVerifier(new TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情况
			}
			return husn;
		}
		return connection;
	}

	/**
	 * 创建连接get
	 *
	 * @return
	 * @throws Exception
	 */
	private HttpURLConnection createGetConnection(String encoding) throws Exception {
		SDKConfig config = SDKConfig.config;

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(this.connectionTimeout);
		connection.setReadTimeout(this.readTimeout);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=" + encoding);
		connection.setRequestMethod("GET");

		if ("https".equalsIgnoreCase(url.getProtocol())) {
			HttpsURLConnection husn = (HttpsURLConnection) connection;
			//是否验证https证书，测试环境请设置false，生产环境建议优先尝试true，不行再false
			if (!config.isIfValidateRemoteCert()) {
				husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
				husn.setHostnameVerifier(new TrustAnyHostnameVerifier());//解决由于服务器证书问题导致HTTPS无法访问的情况
			}
			return husn;
		}
		return connection;
	}
	
	/**
	 * 将Map存储的对象，转换为key=value&key=value的字符
	 *
	 * @param requestParam
	 * @param coder
	 * @return
	 */
	private String getRequestParamString(Map<String, String> requestParam, String coder) throws Exception {
		if (null == coder || "".equals(coder)) {
			coder = "UTF-8";
		}
		StringBuffer sb = new StringBuffer("");
		String reqstr = "";
		if (null != requestParam && 0 != requestParam.size()) {
			for (Entry<String, String> en : requestParam.entrySet()) {
				sb.append(en.getKey());
				sb.append("=");
				if (StringUtils.isNotBlank(en.getValue())) {
					sb.append(URLEncoder.encode(en.getValue(), coder) + "&");
				}
			}
			reqstr = sb.substring(0, sb.length() - 1);
		}
		LogUtil.writeLog("Request Message:[" + reqstr + "]");
		return reqstr;
	}

}
