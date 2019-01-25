package com.emptech.biocollectiononline.manager;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.utils.YModem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import utils.Uart;

public class PowerManager {
    private static final String TAG = "yzq";

    private int YModemCANumber = 0;
    int fd_Uart = 0;
    private boolean isSending = false;
    /*yzq:  55 AA E6 AA 69 01 00 00 */
    private static final byte[] updateStm32AppCmd = new byte[]{(byte) 0x55, (byte) 0xAA, (byte) 0xE6, (byte) 0xAA, (byte) 0x69, (byte) 0x01, (byte) 0x00, (byte) 0x00};
    private byte[] packetYModem = new byte[YModem.PACKET_OVERHEAD + YModem.PACKET_SIZE];
    private Thread threadYModem = null;
    private Handler handlerYModem;
    private Context mContext;

    public PowerManager(Context context, Handler handler) {
        mContext = context;
        handlerYModem = handler;
    }

    public boolean openUart() {
        if (0 >= fd_Uart) {
            fd_Uart = Uart.open("/dev/ttyS3", Uart.O_RDWR);
            if (fd_Uart == -1) {
                LogUtils.e(TAG, "the power serial open error:"+Uart.strerror());
                return false;
            }
           Uart.set_port(fd_Uart, 115200, 8, (byte) 'N', 1);
           LogUtils.e(TAG, "set port:"+Uart.strerror());
           return true;
        }
        return true;
    }

    public boolean update(final String path) {
        File file = new File(path);
        if(!file.exists()){
            return false;
        }
        /*yzq: 先发送升级命令 */
        String str;
        Uart.tcflush(fd_Uart, Uart.TCIOFLUSH);
        int ret = Uart.write(fd_Uart, updateStm32AppCmd, 0, updateStm32AppCmd.length);
        LogUtils.d(TAG, "run: write shake hands cmd: " + Converter.BytesToHexString(updateStm32AppCmd, updateStm32AppCmd.length));
        if (-1 == ret) {
            str = "write shake hands cmd ER: " + Uart.strerror();
            LogUtils.e(TAG, str);
            handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
            return false;
        }
        handlerYModem.obtainMessage(YModem.MSG_INFO, "send updata cmd success").sendToTarget();
        isSending=true;
        threadYModem = new Thread(new Runnable() {

            @Override
            public void run() {
                boolean isRunning = true;
                int ret;
                String str;
                int packets_sent;
                int PackNumber;      //发送文件的包数
                int arrayIndex;
                File file;
                int failTryCount=100;
                byte[] fileName;
                byte[] fileSize;
                InputStream inputStream;
                byte[] readBuf = new byte[YModem.PACKET_SIZE];
                short crc16;

                file = new File(path);
                if (!file.exists()) {
                    str = "File not exit: " + path;
                    LogUtils.e(TAG, str);
                    handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                    return;
                }
                fileName = file.getName().getBytes();
                if (fileName.length > YModem.FILE_NAME_LENGTH) {
                    str = "FileName too long";
                    handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                    return;
                }

                fileSize = Long.toString(file.length()).getBytes();
                if (fileSize.length > YModem.FILE_SIZE_LENGTH) {
                    str = "FileSize too big";
                    LogUtils.e(TAG, "run: " + str);
                    handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                    return;
                }
                str = "Load file: " + file.getName() + " success";
                handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();

                LogUtils.d(TAG, "run: fileName HexDump:" + Converter.BytesToHexString(fileName, fileName.length));

                //计算包数
                PackNumber = (int) (file.length() / YModem.PACKET_SIZE);
                //多出的按一包算
                if (0 != (file.length() % YModem.PACKET_SIZE)) {
                    PackNumber++;
                }
                str = "PackNumber = " + (PackNumber + 1);
                Log.d(TAG, "run: " + str);
                handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();

                str = "Start shake hands ...";
                Log.d(TAG, "run: " + str);
                handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();

                while (true) {
                    if (!isSending) {
                        str = "abort by user";
                        Log.d(TAG, "run: " + str);
                        handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();
                        return;
                    }
                    /*yzq: 先发送升级命令 */
                    Uart.tcflush(fd_Uart, Uart.TCIOFLUSH);
                    ret = Uart.write(fd_Uart, "1".getBytes(), 0, 1);
                    if (-1 == ret) {
                        str = "write shake hands cmd ER: " + Uart.strerror();
                        Log.e(TAG, "run: " + str);
                        handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                        return;
                    }
                    //handlerYModem.obtainMessage(YModem.MSG_INFO, "send 1 success").sendToTarget();

                    ret = Uart.poll_read(fd_Uart, readBuf, 0,readBuf.length,
                            YModem.SHAKE_TIMEOUT);
                    if (-1 == ret) {
                        str = "read shake hands ER: " + Uart.strerror();
                        handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                        return;
                    } else if (0 == ret) {
                        str = "run: read shake hands timeout";
                        LogUtils.d(TAG, str);
                        if (failTryCount <= 0) {
                            handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                            packetYModem[0] = YModem.ABORT1;
                            Uart.write(fd_Uart, packetYModem, 0,1);
                            return;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        failTryCount--;
                        continue;
                    }

                    Log.d(TAG, "run: read shake hands Hex: " + Converter.BytesToHexString(readBuf, ret));
                    /*yzq: 判断最后一个字符是不是CRC16 */
                    if (YModem.CRC16 == readBuf[ret - 1]) {
                        break;
                    } else {
                        /*yzq: 收到不对的数据, 休息一下再继续握手 */
                        SystemClock.sleep(YModem.SHAKE_TIMEOUT);
                    }
                }
                str = "Shake hands success";
                Log.d(TAG, "run: " + str);
                handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();

                try {
                    inputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    str = "get FileInputStream ER, abort transfer";
                    Log.e(TAG, "run: " + str);
                    handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                    packetYModem[0] = YModem.ABORT1;
                    Uart.write(fd_Uart, packetYModem, 0, 1);
                    return;
                }

                /*yzq: -----之后的退出线程方式均采用isRunning = false;continue;退出循环, 不要再用return, 因为要有一个统一点执行inputStream.close()------ */
                str = "Start YModem download ...";
                Log.d(TAG, "run: " + str);
                handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();
                packets_sent = 0;
//                            SystemClock.sleep(10000);
                Uart.tcflush(fd_Uart, Uart.TCIOFLUSH);
                while (isSending && isRunning) {// YModem downloading
                    handlerYModem.obtainMessage(YModem.MSG_PROGRESS, packets_sent, PackNumber + 2).sendToTarget();
                    if (packets_sent <= PackNumber)// packets_sent != 0
                    {
                        // SOH
                        packetYModem[0] = YModem.SOH;
                        packetYModem[YModem.PACKET_SEQNO_INDEX] = (byte) packets_sent;
                        packetYModem[YModem.PACKET_SEQNO_COMP_INDEX] = (byte) (~packetYModem[YModem.PACKET_SEQNO_INDEX]);

                        /*yzq: 接收'C' */
                        if ((0 == packets_sent) || (1 == packets_sent)) {
                            ret = Uart.poll_read(fd_Uart, readBuf, 0, 1, YModem.CRC16_TIMEOUT);
                            if (0 >= ret) {
                                if (ret != 0) {
                                    str = "read 'C' ER: " + Uart.strerror();
                                } else {
                                    str = "read 'C' timeout";
                                }

                                Log.e(TAG, "run: " + str);
                                handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                                packetYModem[0] = YModem.ABORT1;
                                Uart.write(fd_Uart, packetYModem, 0, 1);
                                isRunning = false;
                                continue;
                            }

                            if (YModem.CRC16 != readBuf[0]) {
                                str = "run: read 'C' != " + Converter.BytesToHexString(readBuf, readBuf.length) + ", not cmp";
                                Log.e(TAG, "run: " + str);
                                handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                                packetYModem[0] = YModem.ABORT1;
                                Uart.write(fd_Uart, packetYModem, 0, 1);
                                isRunning = false;
                                continue;
                            }
                            Uart.tcflush(fd_Uart, Uart.TCIOFLUSH);
                        }

                        if (0 == packets_sent) {
                            /*yzq: 文件信息: SOH file_name\0 128 */
                            Arrays.fill(packetYModem, YModem.PACKET_HEADER, packetYModem.length, (byte) 0x00);
                            System.arraycopy(fileName, 0, packetYModem, YModem.PACKET_HEADER, fileName.length);
                            arrayIndex = YModem.PACKET_HEADER + fileName.length;
                            packetYModem[arrayIndex++] = '\0';
                            System.arraycopy(fileSize, 0, packetYModem, arrayIndex, fileSize.length);
                        } else {
                            Arrays.fill(packetYModem, YModem.PACKET_HEADER, packetYModem.length, (byte) 0x1A);
                            try {
//                                            startTime = System.nanoTime();
                                ret = inputStream.read(packetYModem, YModem.PACKET_HEADER, YModem.PACKET_SIZE);
//                                            Log.d(TAG, "run: inputStream.read elapsed time:" + (System.nanoTime() - startTime) / 1000 + "us");
                                if (-1 == ret) {
                                    Log.e(TAG, "run: this never come up!");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                packetYModem[0] = YModem.ABORT1;
                                Uart.write(fd_Uart, packetYModem, 0, 1);
                                isRunning = false;
                                continue;
                            }
                        }

                        /*yzq: 计算CRC */
                        //crc16 = GetCrc(TxData, PACKET_HEADER, PACKET_SIZE);
                        arrayIndex = YModem.PACKET_HEADER + YModem.PACKET_SIZE;
                        crc16 = YModem.Y_Modem_CRC(packetYModem, 3, (short) YModem.PACKET_SIZE);
                        packetYModem[arrayIndex++] = (byte) (crc16 >> 8);
                        packetYModem[arrayIndex++] = (byte) crc16;

                        /*yzq: 发送数据 */
                        Uart.write(fd_Uart, packetYModem, 0, packetYModem.length);
                        Log.d(TAG, "run: Packet[" + packets_sent + "] Hex:" + Converter.BytesToHexString(packetYModem, packetYModem.length));
                    } else if (packets_sent == PackNumber + 1) {
                        // EOT
                        packetYModem[0] = YModem.EOT;
                        Uart.write(fd_Uart, packetYModem, 0, 1);
                        Log.d(TAG, "run: Packet[" + packets_sent + "] Hex:" + Converter.BytesToHexString(packetYModem, 1));
                    } else {
                        // NULL PACKET
                        packetYModem[0] = YModem.SOH;
                        packetYModem[YModem.PACKET_SEQNO_INDEX] = (byte) 0x00;
                        packetYModem[YModem.PACKET_SEQNO_COMP_INDEX] = (byte) (~packetYModem[YModem.PACKET_SEQNO_INDEX]);
                        Arrays.fill(packetYModem, YModem.PACKET_HEADER, packetYModem.length, (byte) 0x00);
                        /*yzq: 计算CRC */
                        //crc16 = GetCrc(TxData, PACKET_HEADER, PACKET_SIZE);
                        arrayIndex = YModem.PACKET_HEADER + YModem.PACKET_SIZE;
                        crc16 = YModem.Y_Modem_CRC(packetYModem, 3, (short) YModem.PACKET_SIZE);
                        packetYModem[arrayIndex++] = (byte) (crc16 >> 8);
                        packetYModem[arrayIndex++] = (byte) crc16;

                        Uart.write(fd_Uart, packetYModem, 0, packetYModem.length);
                        Log.d(TAG, "run: Packet[" + packets_sent + "] Hex:" + Converter.BytesToHexString(packetYModem, packetYModem.length));
                        /*yzq: 正常情况最终从这里退出 */
                        isRunning = false;
                    }

                    /*yzq: 接收ACK */
                    ret = Uart.poll_read(fd_Uart, readBuf, 0, 1, YModem.ACK_TIMEOUT);
                    if (0 >= ret) {
                        if (0 != ret) {
                            str = "read ACK ER:" + Uart.strerror();
                        } else {
                            str = "read ACK TIMEOUT";
                        }
                        Log.d(TAG, "run: " + str);
                        handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                        packetYModem[0] = YModem.ABORT1;
                        Uart.write(fd_Uart, packetYModem, 0, 1);
                        isRunning = false;
                        continue;
                    }
                    if (YModem.ACK == readBuf[0]) {
                        if (isRunning) {
                            packets_sent++;
                        } else {
                            str = "YModem download success";
                            Log.d(TAG, "run: " + str);
                            handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();
                        }

                    } else if ((YModem.CA == readBuf[0]) && (YModemCANumber == 0)) {
                        YModemCANumber++;
                        if (isRunning) {
                            packets_sent++;
                        } else {
                            str = "YModem download success";
                            Log.d(TAG, "run: " + str);
                            handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();
                        }
                    } else {
                        if (YModem.NAK == readBuf[0]) {
                            str = "negative acknowledge";
                        } else if ((YModem.CA == readBuf[0]) && (YModemCANumber == 1)) {
                            /*yzq: 本来是要收到两个CA才会结束传输, 这里为了方便, 收到一个就退出 */
                            str = "receiver aborts transfer";
                        } else if (YModem.CRC16 == readBuf[0]) {
                            str = "receiver check CRC16 ER";
                        } else {
                            str = "unknown ack: 0x" + Converter.BytesToHexString(readBuf, 1);
                        }
                        packetYModem[0] = YModem.ABORT1;
                        Uart.write(fd_Uart, packetYModem, 0, 1);
                        Log.e(TAG, "run: " + str);
                        handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
                        isRunning = false;
                    }
                }

                /*yzq: 关闭文件流 */
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if ((!isSending) && isRunning) {
                    packetYModem[0] = YModem.ABORT1;
                    Uart.write(fd_Uart, packetYModem, 0, 1);
                    str = "abort by user";
                    Log.d(TAG, "run: " + str);
                    handlerYModem.obtainMessage(YModem.MSG_INFO, str).sendToTarget();
                }
            }
        });
        threadYModem.start();
        return true;
    }

    public void update(){
        int ret;
        String str;
        ret = Uart.write(fd_Uart, "3".getBytes(), 0, 1);
        if (-1 == ret) {
            str = "exc pro cmd ER: " + Uart.strerror();
            LogUtils.e(TAG, "run: " + str);
            handlerYModem.obtainMessage(YModem.MSG_ERROR, str).sendToTarget();
        }
    }


    protected void close() {
        if (fd_Uart > 0) {
            if (null != threadYModem) {
                try {
                    threadYModem.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Uart.close(fd_Uart);
        }
    }
}
