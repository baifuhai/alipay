package com.unionpay.acp.sdk;

import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Data
public class SDKConfig {
	// 前台请求URL
	private String frontTransUrl;
	// 后台请求URL
	private String backTransUrl;
	// 单笔查询
	private String singleQueryUrl;
	// 批量查询
	private String batchQueryUrl;
	// 批量交易
	private String batchTransUrl;
	// 文件传输
	private String fileTransUrl;
	// 有卡交易
	private String cardTransUrl;
	// app交易
	private String appTransUrl;

	// 缴费相关地址
	// 前台请求地址
	private String jfFrontTransUrl;
	// 后台请求地址
	private String jfBackTransUrl;
	// 单笔查询请求地址
	private String jfSingleQueryUrl;
	// 有卡交易地址
	private String jfCardTransUrl;
	// App交易地址
	private String jfAppTransUrl;
	// 人到人
	private String qrcBackTransUrl;
	// 人到人
	private String qrcB2cIssBackTransUrl;
	// 人到人
	private String qrcB2cMerBackTransUrl;

	// 综合认证
	// 前台请求地址
	private String zhrzFrontTransUrl;
	// 后台请求地址
	private String zhrzBackTransUrl;
	// 单笔查询请求地址
	private String zhrzSingleQueryUrl;
	// 有卡交易地址
	private String zhrzCardTransUrl;
	// App交易地址
	private String zhrzAppTransUrl;
	// 图片识别交易地址
	private String zhrzFaceTransUrl;

	// 前台通知地址
	private String frontUrl;
	// 后台通知地址，受理方和发卡方自选填写的域[O]
	private String backUrl;
	// 前台失败通知地址
	private String frontFailUrl;

	// 签名方式，没配按01
	private String signMethod = "01";
	// 商户号
	private String merId = "700000000000001";
	// 默认配置的是UTF-8
	private String encoding = "UTF-8";
	// 版本号，没配按5.0.0
	private String version = "5.0.0";

	// 签名证书路径
	private String signCertPath;
	// 签名证书密码
	private String signCertPwd;
	// 签名证书类型
	private String signCertType;

	// 加密公钥证书路径
	private String encryptCertPath;
	// 验证签名公钥证书目录
	private String validateCertDir;
	// 按照商户代码读取指定签名证书目录
	private String signCertDir;

	// 磁道加密证书路径
	private String encryptTrackCertPath;
	// 磁道加密公钥模数
	private String encryptTrackKeyModulus;
	// 磁道加密公钥指数
	private String encryptTrackKeyExponent;

	// 证书使用模式(单证书/多证书)
	private String singleMode;

	// 安全密钥(SHA256和SM3计算时使用)
	private String secureKey;
	// 根证书路径
	private String rootCertPath;
	// 中级证书路径
	private String middleCertPath;
	// 是否验证验签证书CN，除了false之外的值都当true处理
	private boolean ifValidateCNName = true;
	// 是否验证https证书，除了true之外的值都当false处理
	private boolean ifValidateRemoteCert = false;

	// 是否加密cvn2
	private String cvn2Enc = "acpsdk.cvn2.enc";
	// 是否加密cvn2有效期
	private String dateEnc = "acpsdk.date.enc";
	// 是否加密卡号
	private String panEnc = "acpsdk.pan.enc";

	private static SDKConfig config = new SDKConfig();

	public static SDKConfig getInstance() {
		return config;
	}

	private SDKConfig() {
	}

	static {
		try {
			InputStream is = SDKConfig.class.getClassLoader().getResourceAsStream("acp_sdk.properties");
			Properties ps = new Properties();
			ps.load(is);

			String prefix = "acpsdk";

			Set<Map.Entry<Object, Object>> es = ps.entrySet();
			for (Map.Entry<Object, Object> e : es) {
				try {
					String key = e.getKey().toString();
					String value = e.getValue().toString();

					String fieldName = key.replaceFirst("^" + prefix + ".", "");
					BeanUtils.setProperty(config, fieldName, value);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
