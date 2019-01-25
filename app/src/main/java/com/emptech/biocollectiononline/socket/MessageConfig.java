package com.emptech.biocollectiononline.socket;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class MessageConfig {
    public static final byte[] STARTFRAME = new byte[]{0x55, (byte) 0xAA};// 起始帧标记位
    public static final int ReserveFlagLen = 8;//预留的字节长度；
    public static final int headLen = 2 + 4 + 4 + ReserveFlagLen;//包头长度
}
