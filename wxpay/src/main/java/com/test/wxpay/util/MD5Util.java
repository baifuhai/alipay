package com.test.wxpay.util;

import java.security.MessageDigest;

public class MD5Util {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 将字节数组转换为16进制字符串
     *
     * @param bytes 字节数组
     * @return
     */
    public static String byteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(byteToHex(b));
        }
        return sb.toString();
    }

    /**
     * 将字节转换为16进制字符串
     *
     * @param b 字节
     * @return
     */
    private static String byteToHex(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * MD5编码
     *
     * @param s
     * @return
     */
    public static String encode(String s) throws Exception {
        MessageDigest mdInst = MessageDigest.getInstance("MD5");
        byte[] md = mdInst.digest(s.getBytes("UTF-8"));

        return byteToHex(md);
    }

    public static String encode2(String s) throws Exception {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        MessageDigest mdInst = MessageDigest.getInstance("MD5");
        mdInst.update(s.getBytes("UTF-8"));
        byte[] md = mdInst.digest();

        int j = md.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    public static String encode3(String s) throws Exception {
        MessageDigest mdInst = MessageDigest.getInstance("MD5");
        mdInst.update(s.getBytes("UTF-8"));
        byte[] md = mdInst.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < md.length; i++) {
            String hex = Integer.toHexString(md[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制字符串转换成字节数组
     *
     * @param s
     * @return
     */
    private static byte[] hexToByte(String s) throws Exception {
        if (s.length() % 2 == 0) {
            return hexToByte(s.getBytes("UTF-8"), 0, s.length() >> 1);
        } else {
            return hexToByte("0" + s);
        }
    }
    private static byte[] hexToByte(byte[] b, int offset, int len) {
        byte[] d = new byte[len];
        for (int i = 0; i < len * 2; i++) {
            int shift = i % 2 == 1 ? 0 : 4;
            d[i>>1] |= Character.digit((char) b[offset+i], 16) << shift;
        }
        return d;
    }

}
