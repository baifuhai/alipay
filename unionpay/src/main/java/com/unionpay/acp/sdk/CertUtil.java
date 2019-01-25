package com.unionpay.acp.sdk;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 证书工具类，主要用于对证书的加载和使用
 */
public class CertUtil {

	/** 证书容器，存储对商户请求报文签名私钥证书. */
	private static KeyStore keyStore = null;

	/** 验签中级证书 */
	private static X509Certificate middleCert = null;

	/** 验签根证书 */
	private static X509Certificate rootCert = null;

	/** 敏感信息加密公钥证书 */
	private static X509Certificate encryptCert = null;

	/** 磁道加密公钥 */
	private static PublicKey encryptTrackKey = null;

	/** 验证银联返回报文签名的公钥证书存储Map. */
	private static final Map<String, X509Certificate> certMap = new HashMap<String, X509Certificate>();

	private static final SDKConfig config = SDKConfig.getInstance();

	static {
		init();
	}
	
	/**
	 * 初始化所有证书.
	 */
	private static void init() {
		try {
			// 向系统添加BC provider
			addProvider();

			// 签名私钥证书
			genSignCert();

			// 银联验签公钥中级证书
			middleCert = genCert(config.getMiddleCertPath());

			// 银联验签公钥根证书
			rootCert = genCert(config.getRootCertPath());

			// 加密公钥证书
			encryptCert = genCert(config.getEncryptCertPath());

			// 磁道加密公钥
			//encryptTrackKey = getPublicKey(config.getEncryptTrackKeyModulus(), config.getEncryptTrackKeyExponent());

			// 初始化目录里的验签证书
			//initCertFromDir();
		} catch (Exception e) {
			LogUtil.writeErrorLog("init失败。（如果是用对称密钥签名的可无视此异常。）", e);
		}
	}
	
	/**
	 * 添加签名，验签，加密算法提供者
	 */
	private static void addProvider(){
		if (Security.getProvider("BC") == null) {
			LogUtil.writeLog("add BC provider");
			Security.addProvider(new BouncyCastleProvider());
		} else {
			LogUtil.writeLog("re-add BC provider");
			Security.removeProvider("BC");//解决eclipse调试时tomcat自动重新加载时，BC存在不明原因异常的问题
			Security.addProvider(new BouncyCastleProvider());
		}
		printSysInfo();
	}
	
	/**
	 * 加载签名证书
	 */
	private static void genSignCert() throws Exception {
		if (!"01".equals(config.getSignMethod())) {
			LogUtil.writeLog("非rsa签名方式，不加载签名证书。");
			return;
		}

		keyStore = genKeyStore(config.getSignCertPath(), config.getSignCertPwd(), config.getSignCertType());
	}

	/**
	 * 加载验证签名证书
	 */
	private static void initCertFromDir() {
		if(!"01".equals(config.getSignMethod())){
			LogUtil.writeLog("非rsa签名方式，不加载验签证书。");
			return;
		}

		String dir = config.getValidateCertDir();
		LogUtil.writeLog("加载验证签名证书目录==>" + dir +" 注：如果请求报文中version=5.1.0那么此验签证书目录使用不到，可以不需要设置（version=5.0.0必须设置）。");
		if (StringUtils.isBlank(dir)) {
			LogUtil.writeErrorLog("WARN: acpsdk.validateCert.dir is empty");
			return;
		}

		File fileDir = new File(dir);
		File[] files = fileDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			try {
				File file = files[i];
				if (file.getName().toLowerCase().endsWith(".cer")) {
					X509Certificate cert = genCert(file.getAbsolutePath());
					certMap.put(cert.getSerialNumber().toString(), cert);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static X509Certificate genCert(String path) throws Exception {
		InputStream in = null;
		try {
			if (path.startsWith("classpath:")) {
				in = CertUtil.class.getClassLoader().getResourceAsStream(path.replaceFirst("^classpath:", ""));
			} else {
				in = new FileInputStream(path);
			}
			return genCert(in);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static X509Certificate genCertByStr(String x509CertString) throws Exception {
		InputStream in = new ByteArrayInputStream(x509CertString.getBytes("ISO-8859-1"));
		return genCert(in);
	}
	private static X509Certificate genCert(InputStream in) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
		return cert;
	}

	public static PrivateKey getSignCertPrivateKey() throws Exception {
		return getSignCertPrivateKey(keyStore, config.getSignCertPwd());
	}
	public static PrivateKey getSignCertPrivateKey(String certPath, String password) throws Exception {
		KeyStore keyStore = genKeyStore(certPath, password, "PKCS12");
		return getSignCertPrivateKey(keyStore, password);
	}
	public static PrivateKey getSignCertPrivateKey(KeyStore keyStore, String password) throws Exception {
		Enumeration<String> aliasesEnum = keyStore.aliases();
		if (aliasesEnum.hasMoreElements()) {
			String keyAlias = aliasesEnum.nextElement();
			PrivateKey key = (PrivateKey) keyStore.getKey(keyAlias, password.toCharArray());
			return key;
		}
		return null;
	}

	public static String getSignCertId() throws Exception {
		return getSignCertId(keyStore);
	}
	public static String getSignCertId(String certPath, String certPwd) throws Exception {
		KeyStore keyStore = genKeyStore(certPath, certPwd, "PKCS12");
		return getSignCertId(keyStore);
	}
	private static String getSignCertId(KeyStore keyStore) throws Exception {
		Enumeration<String> aliasesEnum = keyStore.aliases();
		if (aliasesEnum.hasMoreElements()) {
			String keyAlias = aliasesEnum.nextElement();
			X509Certificate cert = (X509Certificate) keyStore.getCertificate(keyAlias);
			return cert.getSerialNumber().toString();
		}
		return null;
	}

	/**
	 * 将签名私钥证书文件读取为证书存储对象
	 * 
	 * @param pfxKeyFile 证书文件名
	 * @param password 证书密码
	 * @param type 证书类型
	 * @return
	 * @throws Exception
	 */
	private static KeyStore genKeyStore(String pfxKeyFile, String password, String type) throws Exception {
		InputStream in = null;
		try {
			if (pfxKeyFile.startsWith("classpath:")) {
				in = CertUtil.class.getClassLoader().getResourceAsStream(pfxKeyFile.replaceFirst("^classpath:", ""));
			} else {
				in = new FileInputStream(pfxKeyFile);
			}
			KeyStore keyStore = KeyStore.getInstance(type, "BC");
			keyStore.load(in, password.toCharArray());
			return keyStore;
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
		}
	}
	
	/**
	 * 使用模和指数生成RSA公钥
	 * 注意：此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同
	 * 
	 * @param modulus 模
	 * @param exponent 指数
	 * @return
	 */
	private static PublicKey getPublicKey(String modulus, String exponent) throws Exception {
		BigInteger b1 = new BigInteger(modulus);
		BigInteger b2 = new BigInteger(exponent);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 获取证书的CN
	 *
	 * @param cert
	 * @return
	 */
	private static String getIdentitiesFromCertificate(X509Certificate cert) {
		String tDN = cert.getSubjectDN().toString();
		String tPart = "";
		if ((tDN != null)) {
			String tSplitStr[] = tDN.substring(tDN.indexOf("CN=")).split("@");
			if (tSplitStr != null && tSplitStr.length > 2 && tSplitStr[2] != null) {
				tPart = tSplitStr[2];
			}
		}
		return tPart;
	}
	
	/**
	 * 验证证书链
	 *
	 * @param cert
	 * @return
	 */
	private static boolean verifyCertificateChain(X509Certificate cert) throws Exception {
		if (null == cert) {
			LogUtil.writeErrorLog("cert must Not null");
			return false;
		}
		
		X509Certificate middleCert = CertUtil.getMiddleCert();
		if (null == middleCert) {
			LogUtil.writeErrorLog("middleCert must Not null");
			return false;
		}
		
		X509Certificate rootCert = CertUtil.getRootCert();
		if (null == rootCert) {
			LogUtil.writeErrorLog("rootCert or cert must Not null");
			return false;
		}
		
		try {
	        X509CertSelector selector = new X509CertSelector();
	        selector.setCertificate(cert);
	        
	        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
	        trustAnchors.add(new TrustAnchor(rootCert, null));
	        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
	
	        Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
	        intermediateCerts.add(rootCert);
	        intermediateCerts.add(middleCert);
	        intermediateCerts.add(cert);
	        
	        pkixParams.setRevocationEnabled(false);
	
	        CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateCerts), "BC");
	        pkixParams.addCertStore(intermediateCertStore);
	
	        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
	        
			PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) builder.build(pkixParams);
			LogUtil.writeLog("verify certificate chain succeed.");
			return true;
        } catch (java.security.cert.CertPathBuilderException e){
			LogUtil.writeErrorLog("verify certificate chain fail.", e);
		} catch (Exception e) {
			LogUtil.writeErrorLog("verify certificate chain exception: ", e);
		}
		return false;
	}
	
	/**
	 * 检查证书链
	 * 
	 * @param cert 待验证的证书
	 * @return
	 */
	public static boolean verifyCertificate(X509Certificate cert) {
		try {
			cert.checkValidity();//验证有效期
//			cert.verify(middleCert.getPublicKey());
			if (!verifyCertificateChain(cert)) {
				return false;
			}
		} catch (Exception e) {
			LogUtil.writeErrorLog("verifyCertificate fail", e);
			return false;
		}
		
		if (config.isIfValidateCNName()) {
			// 验证公钥是否属于银联
			if(!SDKConstants.UNIONPAY_CNNAME.equals(CertUtil.getIdentitiesFromCertificate(cert))) {
				LogUtil.writeErrorLog("cer owner is not CUP:" + CertUtil.getIdentitiesFromCertificate(cert));
				return false;
			}
		} else {
			// 验证公钥是否属于银联
			if(!SDKConstants.UNIONPAY_CNNAME.equals(CertUtil.getIdentitiesFromCertificate(cert))
					&& !"00040000:SIGN".equals(CertUtil.getIdentitiesFromCertificate(cert))) {
				LogUtil.writeErrorLog("cer owner is not CUP:" + CertUtil.getIdentitiesFromCertificate(cert));
				return false;
			}
		}
		return true;		
	}

	/**
	 * 打印系统环境信息
	 */
	private static void printSysInfo() {
		LogUtil.writeLog("================= SYS INFO begin ====================");
		LogUtil.writeLog("os_name:" + System.getProperty("os.name"));
		LogUtil.writeLog("os_arch:" + System.getProperty("os.arch"));
		LogUtil.writeLog("os_version:" + System.getProperty("os.version"));
		LogUtil.writeLog("java_vm_specification_version:" + System.getProperty("java.vm.specification.version"));
		LogUtil.writeLog("java_vm_specification_vendor:" + System.getProperty("java.vm.specification.vendor"));
		LogUtil.writeLog("java_vm_specification_name:" + System.getProperty("java.vm.specification.name"));
		LogUtil.writeLog("java_vm_version:" + System.getProperty("java.vm.version"));
		LogUtil.writeLog("java_vm_name:" + System.getProperty("java.vm.name"));
		LogUtil.writeLog("java.version:" + System.getProperty("java.version"));
		LogUtil.writeLog("java.vm.vendor=[" + System.getProperty("java.vm.vendor") + "]");
		LogUtil.writeLog("java.version=[" + System.getProperty("java.version") + "]");
		printProviders();
		LogUtil.writeLog("================= SYS INFO end =====================");
	}
	
	/**
	 * 打jre中印算法提供者列表
	 */
	private static void printProviders() {
		LogUtil.writeLog("Providers List:");
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++) {
			LogUtil.writeLog(i + 1 + "." + providers[i].getName());
		}
	}

	public static KeyStore getKeyStore() {
		return keyStore;
	}

	public static X509Certificate getMiddleCert() {
		return middleCert;
	}

	public static X509Certificate getRootCert() {
		return rootCert;
	}

	public static X509Certificate getEncryptCert() {
		return encryptCert;
	}

	public static PublicKey getEncryptTrackPublicKey() {
		return encryptTrackKey;
	}

	public static String getEncryptCertId() {
		return encryptCert.getSerialNumber().toString();
	}

	public static void resetEncryptCertPublicKey() {
		encryptCert = null;
	}

	public static PublicKey getValidatePublicKey(String certId) {
		return certMap.get(certId).getPublicKey();
	}

}
