package com.cardlan.carddemoapp.util;

import com.cardlan.utils.ByteUtil;

/**
 * @Description: java类作用描述
 * @Author: jakezhang
 * @CreateDate: 2020/11/22 18:57
 */
public class CardReadWriteUtil2 {

    //字节数组按位取反，并转为16进制字符串输出
    public static String byteNotHexStr(byte[] bytes) {
        String bytesNotHexStr = "";
        byte[] bytesNot = new byte[32];//存放32位字节数组取反的值
        for (int i = 0; i < bytes.length; i++) {
            bytesNot[i] = (byte) ~bytes[i];
        }
        bytesNotHexStr = ByteUtil.byteArrayToHexString(bytesNot);
        return bytesNotHexStr;
    }

    //转为char
    public static char stringToChar(String string) {
        if (!ByteUtil.notNull(string)) {
            string = "0";
        }
//        int ivalue = Integer.parseInt(string);
//        return (char) ivalue;
        return ByteUtil.intStringToChar(Integer.valueOf(string,16).toString());
    }

    //int转byte数组 地位在前
    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    //int转byte数组 高位在前
    public static byte[] toHH(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

}
