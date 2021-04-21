/*
 * Copyright (c) 2020. Engineer-Jsp
 *
 * Any document of this project is owned by Engineer-Jsp;
 * Without the permission of the company, it is forbidden to send any
 * documents of this project to anyone who has nothing to do with the project.
 * About the project author , You can visit he's other open source projects or he's blog
 *
 * CSDN   : https://blog.csdn.net/jspping
 * GitHub : https://github.com/Mr-Jiang
 *
 * Once again explanation, it is forbidden to disclose any documents of the project
 * to anyone who has nothing to do with the project without the permission of
 * the company otherwise legal liability will be pursued according to law.
 */
package com.android.aacdecoder.utils;

/**
 * utils class.
 *
 * @author : Engineer-Jsp
 * @date : Created by 2021/1/29 16:58
 */
public class Utils {

    /**
     * BYTE[n] convert to hex string , no "0x" char
     *
     * @param src byte array
     * @return no "0x" char byte array hex string
     */
    public static String bytesToHexStringNo0xChar(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

}
