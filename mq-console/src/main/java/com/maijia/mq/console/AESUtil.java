package com.maijia.mq.console;

import java.security.Key;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description:AES加密工具
 */
public class AESUtil {
    /**
     * 日志
     */
    private static final Log logger = LogFactory.getLog(AESUtil.class);
    /**
     * 公钥
     */
    private static String key = "94CAE2978E484FAF";

    public static byte[] encrypt(byte[] str, byte[] keys) throws Exception {
        Key skey = new javax.crypto.spec.SecretKeySpec(keys, "AES");
        Cipher encryptCipher;
        encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, skey);
        return encryptCipher.doFinal(str);
    }

    public static byte[] decrypt(byte[] str, byte[] keys) throws Exception {
        Key skey = new javax.crypto.spec.SecretKeySpec(keys, "AES");
        Cipher decryptCipher;
        decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, skey);
        return decryptCipher.doFinal(str);
    }

    public static String byte2Hex(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;

        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10) {
                strbuf.append("0");
            }
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }

        return strbuf.toString();
    }

    public static byte[] hex2Byte(String src) {
        if (src.length() < 1) {
            return src.getBytes();
        }
        byte[] encrypted = new byte[src.length() / 2];
        for (int i = 0; i < src.length() / 2; i++) {
            int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);

            encrypted[i] = (byte) (high * 16 + low);
        }
        return encrypted;
    }

    /**
     * Description:公钥加密 Date:2013-2-18
     *
     * @param data
     * @return String
     * @throws Exception
     */
    public static String encryptStr(String data) throws Exception {
        return AESUtil.byte2Hex(AESUtil.encrypt(data.getBytes(), key.getBytes()));
    }

    /**
     * Description:公钥解密 Date:2013-2-18
     *
     * @param data
     * @return String
     * @throws Exception
     */
    public static String decryptStr(String data) throws Exception {
        return new String(AESUtil.decrypt(AESUtil.hex2Byte(data), key.getBytes()));
    }

    /**
     * Description:私钥加密 Date:2013-2-18
     *
     * @param data
     * @param privateKey
     * @return String
     * @throws Exception
     */
    public static String encryptStr(String data, String privateKey) throws Exception {
        return AESUtil.byte2Hex(AESUtil.encrypt(data.getBytes(), privateKey.getBytes()));
    }

    /**
     * Description:私钥解密 Date:2013-2-18
     *
     * @param data
     * @param privateKey
     * @return String
     * @throws Exception
     */
    public static String decryptStr(String data, String privateKey) throws Exception {
        return new String(AESUtil.decrypt(AESUtil.hex2Byte(data), privateKey.getBytes()));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(AESUtil.encryptStr("SGCC_DGH"));
        System.out.println(AESUtil.encryptStr("2d89569b-34d5-4c83-990c-232b63c9fb1e", "CB5ACE2A1D9D4C6E"));
        System.out.println(AESUtil.decryptStr("4a60d3c640ebf8a7127273e4231f4dc342f5df1268ebb8eaabc63cedbc2fa8c0d82e2fc411b315e1747abb6edcbdd204", "CB5ACE2A1D9D4C6E"));
    }
}
