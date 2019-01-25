package com.emptech.biocollectiononline.utils;

/**
 * Created by yzq on 2017-01-05.
 */

public class YModem {
    /*yzq: copy from ymodem.h */
    public static final int PACKET_SEQNO_INDEX = (1);
    public static final int PACKET_SEQNO_COMP_INDEX = (2);

    public static final int PACKET_HEADER = (3);
    public static final int PACKET_TRAILER = (2);
    public static final int PACKET_OVERHEAD = (PACKET_HEADER + PACKET_TRAILER);
    public static final int PACKET_SIZE = (128);
    public static final int PACKET_1K_SIZE = (1024);

    public static final int FILE_NAME_LENGTH = (256);
    public static final int FILE_SIZE_LENGTH = (16);

    public static final byte SOH = (0x01); /* start of 128-byte data packet */
    public static final byte STX = (0x02); /* start of 1024-byte data packet */
    public static final byte EOT = (0x04); /* end of transmission */
    public static final byte ACK = (0x06);  /* acknowledge */
    public static final byte NAK = (0x15);  /* negative acknowledge */
    public static final byte CA = (0x18); /* two of these in succession aborts transfer */
    public static final byte CRC16 = (0x43);  /* 'C' == 0x43, request 16-bit CRC */

    public static final byte ABORT1 = (0x41);  /* 'A' == 0x41, abort by user */
    public static final byte ABORT2 = (0x61);  /* 'a' == 0x61, abort by user */

    public static final int NAK_TIMEOUT = (0x100000);
    public static final int MAX_ERRORS = (5);

    /*yzq: add for read timeout */
    public static final int CRC16_TIMEOUT = (5000);// ms
    public static final int ACK_TIMEOUT = (5000);// ms
    public static final int SHAKE_TIMEOUT = (100);// ms

    /*yzq: add for YModem handler */
    public static final int MSG_PROGRESS = 1990;
    public static final int MSG_ERROR = 1991;
    public static final int MSG_INFO = 1992;
    public static final int MSG_END = 1993;

    public static short Y_Modem_CRC(byte[] buf, int offset, int len) {
        short chsum;
        int stat;
        short i;
        int in_ptr;

        //指向要计算CRC的缓冲区开头
        in_ptr = offset;
        chsum = 0;
        for (stat = len; stat > 0; stat--) //len是所要计算的长度
        {
            chsum = (short) ((chsum & 0xFFFF) ^ ((buf[in_ptr++] & 0xFF) << 8));
            for (i = 8; i != 0; i--) {
                if (0 != (chsum & 0x8000)) {
                    chsum = (short) ((chsum & 0xFFFF) << 1 ^ 0x1021);
                } else {
                    chsum = (short) ((chsum & 0xFFFF) << 1);
                }
            }
        }
        return chsum;
    }
}
