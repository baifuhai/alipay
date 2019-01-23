package com.unionpay.acp.sdk;

import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.digests.SM3Digest;

/**
 * 报文加密解密等操作的工具类
 */
public class SecureUtil {

	private static final String ALGORITHM_SHA1 = "SHA-1";

	private static final String ALGORITHM_SHA256 = "SHA-256";

	private static final String BC_PROV_ALGORITHM_SHA1RSA = "SHA1withRSA";

	private static final String BC_PROV_ALGORITHM_SHA256RSA = "SHA256withRSA";

	private static final String ALGORITHM_MODE_PADDING = "RSA/ECB/PKCS1Padding";

	/**
	 * sm3计算后进行16进制转换
	 * 
	 * @param data 待计算的数据
	 * @param encoding 编码
	 * @return
	 */
	public static String sm3X16Str(String data, String encoding) throws Exception {
		byte[] bytes = sm3(data.getBytes(encoding));
		StringBuilder sm3StrBuff = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (Integer.toHexString(0xFF & bytes[i]).length() == 1) {
				sm3StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
			} else {
				sm3StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		return sm3StrBuff.toString();
	}

	/**
	 * sha1计算后进行16进制转换
	 * 
	 * @param data 待计算的数据
	 * @param encoding 编码
	 * @return
	 */
	public static byte[] sha1X16(String data, String encoding) throws Exception {
		byte[] bytes = sha1(data.getBytes(encoding));
		StringBuilder sha1StrBuff = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (Integer.toHexString(0xFF & bytes[i]).length() == 1) {
				sha1StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
			} else {
				sha1StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		try {
			return sha1StrBuff.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			LogUtil.writeErrorLog(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * sha256计算后进行16进制转换
	 * 
	 * @param data 待计算的数据
	 * @param encoding 编码
	 * @return
	 */
	public static String sha256X16Str(String data, String encoding) throws Exception {
		byte[] bytes = sha256(data.getBytes(encoding));
		StringBuilder sha256StrBuff = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (Integer.toHexString(0xFF & bytes[i]).length() == 1) {
				sha256StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
			} else {
				sha256StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		return sha256StrBuff.toString();
	}
	
	/**
	 * sha256计算后进行16进制转换
	 *
	 * @param data 待计算的数据
	 * @param encoding 编码
	 * @return
	 * @throws Exception
	 */
	public static byte[] sha256X16(String data, String encoding) throws Exception {
		byte[] bytes = sha256(data.getBytes(encoding));
		StringBuilder sha256StrBuff = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if (Integer.toHexString(0xFF & bytes[i]).length() == 1) {
				sha256StrBuff.append("0").append(Integer.toHexString(0xFF & bytes[i]));
			} else {
				sha256StrBuff.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		return sha256StrBuff.toString().getBytes(encoding);
	}

	/**
	 * sha1计算
	 *
	 * @param data 待计算的数据
	 * @return
	 * @throws Exception
	 */
	private static byte[] sha1(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA1);
		md.reset();
		md.update(data);
		return md.digest();
	}

	/**
	 * sha256计算
	 *
	 * @param data 待计算的数据
	 * @return
	 * @throws Exception
	 */
	private static byte[] sha256(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
		md.reset();
		md.update(data);
		return md.digest();
	}

	/**
	 * sm3计算
	 *
	 * @param data 待计算的数据
	 * @return
	 */
	private static byte[] sm3(byte[] data) {
		SM3Digest sm3 = new SM3Digest();
		sm3.update(data, 0, data.length);
		byte[] result = new byte[sm3.getDigestSize()];
		sm3.doFinal(result, 0);
		return result;
	}
	
	public static byte[] signBySoft(PrivateKey privateKey, byte[] data) throws Exception {
		Signature st = Signature.getInstance(BC_PROV_ALGORITHM_SHA1RSA, "BC");
		st.initSign(privateKey);
		st.update(data);
		return st.sign();
	}
	
	public static byte[] signBySoft256(PrivateKey privateKey, byte[] data) throws Exception {
		Signature st = Signature.getInstance(BC_PROV_ALGORITHM_SHA256RSA, "BC");
		st.initSign(privateKey);
		st.update(data);
		return st.sign();
	}

	public static boolean validateSignBySoft(PublicKey publicKey, byte[] signData, byte[] srcData) throws Exception {
		Signature st = Signature.getInstance(BC_PROV_ALGORITHM_SHA1RSA, "BC");
		st.initVerify(publicKey);
		st.update(srcData);
		return st.verify(signData);
	}
	
	public static boolean validateSignBySoft256(PublicKey publicKey, byte[] signData, byte[] srcData) throws Exception {
		Signature st = Signature.getInstance(BC_PROV_ALGORITHM_SHA256RSA, "BC");
		st.initVerify(publicKey);
		st.update(srcData);
		return st.verify(signData);
	}

	/**
	 * 公钥加密，base64编码
	 *
	 * @param accNo 待处理数据
	 * @param encoding 字符编码
	 * @param key 公钥
	 * @return
	 * @throws Exception
	 */
	public static String encryptPin(String accNo, String pin, String encoding, PublicKey key) throws Exception {
		byte[] data = pin2PinBlockWithCardNO(pin, accNo);
		data = encryptData(key, data);
		return new String(SecureUtil.base64Encode(data), encoding);
	}

	/**
	 * 公钥加密，base64编码
	 * 
	 * @param dataString 待处理数据
	 * @param encoding 字符编码
	 * @param key 公钥
	 * @return
	 * @throws Exception
	 */
	public static String encryptData(String dataString, String encoding, PublicKey key) throws Exception {
		byte[] data = encryptData(key, dataString.getBytes(encoding));
		return new String(SecureUtil.base64Encode(data), encoding);
	}

	/**
	 * base64解码，私钥解密
	 * 
	 * @param dataString base64过的数据
	 * @param encoding 编码
	 * @param key 私钥
	 * @return
	 * @throws Exception
	 */
	public static String decryptData(String dataString, String encoding, PrivateKey key) throws Exception {
		byte[] data = SecureUtil.base64Decode(dataString.getBytes(encoding));
		data = decryptData(key, data);
		return new String(data, encoding);
	}

	/**
	 * base64编码
	 * 
	 * @param data 待编码数据
	 * @return
	 */
	public static byte[] base64Encode(byte[] data) {
		return Base64.encodeBase64(data);
	}

	/**
	 * base64解码
	 *
	 * @param data 待解码数据
	 * @return
	 */
	public static byte[] base64Decode(byte[] data) {
		return Base64.decodeBase64(data);
	}

	/**
	 * 加密
	 * 加密除pin之外的其他信息
	 * 
	 * @param publicKey
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static byte[] encryptData(PublicKey publicKey, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING,"BC");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 *
	 * @param privateKey
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static byte[] decryptData(PrivateKey privateKey, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING,"BC");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}

	private static byte[] pin2PinBlock(String aPin) {
		int tTemp = 1;
		int tPinLen = aPin.length();

		byte[] tByte = new byte[8];
		try {
			/*******************************************************************
			 * if (tPinLen > 9) { tByte[0] = (byte) Integer.parseInt(new
			 * Integer(tPinLen) .toString(), 16); } else { tByte[0] = (byte)
			 * Integer.parseInt(new Integer(tPinLen) .toString(), 10); }
			 ******************************************************************/
//			tByte[0] = (byte) Integer.parseInt(new Integer(tPinLen).toString(),
//					10);
			tByte[0] = (byte) Integer.parseInt(Integer.toString(tPinLen), 10);
			if (tPinLen % 2 == 0) {
				for (int i = 0; i < tPinLen;) {
					String a = aPin.substring(i, i + 2);
					tByte[tTemp] = (byte) Integer.parseInt(a, 16);
					if (i == (tPinLen - 2)) {
						if (tTemp < 7) {
							for (int x = (tTemp + 1); x < 8; x++) {
								tByte[x] = (byte) 0xff;
							}
						}
					}
					tTemp++;
					i = i + 2;
				}
			} else {
				for (int i = 0; i < tPinLen - 1;) {
					String a;
					a = aPin.substring(i, i + 2);
					tByte[tTemp] = (byte) Integer.parseInt(a, 16);
					if (i == (tPinLen - 3)) {
						String b = aPin.substring(tPinLen - 1) + "F";
						tByte[tTemp + 1] = (byte) Integer.parseInt(b, 16);
						if ((tTemp + 1) < 7) {
							for (int x = (tTemp + 2); x < 8; x++) {
								tByte[x] = (byte) 0xff;
							}
						}
					}
					tTemp++;
					i = i + 2;
				}
			}
		} catch (Exception e) {
		}

		return tByte;
	}

	private static byte[] formatPan(String aPan) {
		int tPanLen = aPan.length();
		byte[] tByte = new byte[8];

		int temp = tPanLen - 13;
		try {
			tByte[0] = (byte) 0x00;
			tByte[1] = (byte) 0x00;
			for (int i = 2; i < 8; i++) {
				String a = aPan.substring(temp, temp + 2);
				tByte[i] = (byte) Integer.parseInt(a, 16);
				temp = temp + 2;
			}
		} catch (Exception e) {
		}
		return tByte;
	}

	public static byte[] pin2PinBlockWithCardNO(String aPin, String aCardNO) {
		byte[] tPinByte = pin2PinBlock(aPin);
		if (aCardNO.length() == 11) {
			aCardNO = "00" + aCardNO;
		} else if (aCardNO.length() == 12) {
			aCardNO = "0" + aCardNO;
		}
		byte[] tPanByte = formatPan(aCardNO);
		byte[] tByte = new byte[8];
		for (int i = 0; i < 8; i++) {
			tByte[i] = (byte) (tPinByte[i] ^ tPanByte[i]);
		}
		return tByte;
	}
	
	/**
     * luhn算法
     *
     * @param number
     * @return
     */
	public static int genLuhn(String number) {
        number = number + "0";
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {// this is for odd digits, they are 1-indexed in
                             // the algorithm
                s1 += digit;
            } else {// add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        int check = 10 - ((s1 + s2) % 10);
        if (check == 10) {
			check = 0;
		}
		return check;
    }

}
