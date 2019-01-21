package com.test.wxpay.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.Base64;

public class Base64Util {

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String s) {
        return Base64.getDecoder().decode(s);
    }

    public static String encode2(byte[] data) {
        return new BASE64Encoder().encode(data);
    }

    public static byte[] decode2(String s) throws Exception {
        return new BASE64Decoder().decodeBuffer(s);
    }

    public static String encode3(byte[] data) {
        return new String(new org.apache.commons.codec.binary.Base64().encode(data));
    }

    public static byte[] decode3(String s) {
        return new org.apache.commons.codec.binary.Base64().decode(s);
    }

}
