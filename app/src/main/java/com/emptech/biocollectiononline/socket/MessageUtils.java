package com.emptech.biocollectiononline.socket;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.utils.YModem;

import java.util.Arrays;

import static com.emptech.biocollectiononline.socket.MessageConfig.STARTFRAME;
import static com.emptech.biocollectiononline.socket.MessageConfig.headLen;

/**
 * 传输包工具
 */

public class MessageUtils {

    /**
     * 获取包的传输类型
     *
     * @param Data 包
     * @return
     */
    public static MessageType.TYPE_MESSAGE getTypeByRecData(byte[] Data) {
        if (!checkByteLegitimate(Data)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "校验未通过");
            return MessageType.TYPE_MESSAGE.ERROR;
        }
        byte[] type = new byte[2];
        byte[] typePCResponse = new byte[2];
        System.arraycopy(Data, headLen, type, 0, type.length);
        System.arraycopy(Data, headLen + 1, typePCResponse, 0, typePCResponse.length);
        if (Arrays.equals(type, MessageType.PC_TYPE.TYPE_USER)) {  //"显示用户信息"
            return MessageType.TYPE_MESSAGE.USER;
        } else if (Arrays.equals(type, MessageType.PC_TYPE.TYPE_START_ACTION_COLLECTION)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "开始采集包");
            return MessageType.TYPE_MESSAGE.START_ACTION_COLLECTION;
        } else if (Arrays.equals(type, MessageType.PC_TYPE.TYPE_START_ACTION_MODE)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "启动模块包");
            return MessageType.TYPE_MESSAGE.START_ACTION_MODE;
        }else if (Arrays.equals(type, MessageType.PC_TYPE.TYPE_START_CHECK_COLLECTION)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "申请校验包");
            return MessageType.TYPE_MESSAGE.START_CHECK_COLLECTION;
        }else if (Arrays.equals(type, MessageType.PC_TYPE.TYPE_PREVIEW)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "预览包");
            return MessageType.TYPE_MESSAGE.PREVIEW;
        }  else if (Arrays.equals(typePCResponse, MessageType.PC_TYPE.TYPE_PHOTO)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "人像确认包");
            return MessageType.TYPE_MESSAGE.PHOTO;
        } else if (Arrays.equals(typePCResponse, MessageType.PC_TYPE.TYPE_CHECK_ACTION_COLLECTION)) {
            LogUtils.v(AppConfig.MODULE_SERVER, "校验结果包");
            return MessageType.TYPE_MESSAGE.CHECK_COLLECTION;
        }else if(Arrays.equals(type,MessageType.PC_TYPE.TYPE_ALL_INFO)){
            LogUtils.v(AppConfig.MODULE_SERVER, "显示所有信息包");
            return MessageType.TYPE_MESSAGE.ALL_INFO;
        }else if(Arrays.equals(type, MessageType.PC_TYPE.TYPE_CLOSE_PREVIEW)){
            LogUtils.v(AppConfig.MODULE_SERVER, "关闭视频预览包");
            return MessageType.TYPE_MESSAGE.CLOSE_VIDEO;
        }else if(Arrays.equals(type,MessageType.PC_TYPE.TYPE_HEART )){
            LogUtils.v(AppConfig.MODULE_SERVER, "心跳检测包");
            return MessageType.TYPE_MESSAGE.HEART;
        }else if(Arrays.equals(type,MessageType.PC_TYPE.TYPE_VERSION )){
            LogUtils.v(AppConfig.MODULE_SERVER, "获取版本包");
            return MessageType.TYPE_MESSAGE.GETVERSION;
        }else if(Arrays.equals(type,MessageType.PC_TYPE.TYPE_LED )){
            LogUtils.v(AppConfig.MODULE_SERVER, "调整LED灯");
            return MessageType.TYPE_MESSAGE.LED;
        }
        LogUtils.v(AppConfig.MODULE_SERVER, "校验类型未通过：类型一："+ Converter.BytesToHexString(type,type.length)+";类型二："+ Converter.BytesToHexString(typePCResponse,typePCResponse.length));
        return MessageType.TYPE_MESSAGE.ERROR;
    }

    /**
     * 判定是否符合协议标准（通用）
     *
     * @param data 判定的数据
     * @return 是否符合
     */
    public static boolean checkByteLegitimate(byte[] data) {
        if (data == null) {
            LogUtils.e(AppConfig.MODULE_SERVER, "the data is  null");
            return false;
        }
        int dataLength = data.length;
        if (dataLength < headLen) {
            // 长度不对，判定失败
            LogUtils.e(AppConfig.MODULE_SERVER, " the length of data is wrong，recieve fail");
            return false;
        }
        if ((data[0] & 0xFF) != (STARTFRAME[0] & 0xFF)
                || (data[1] & 0xFF) != (STARTFRAME[1] & 0xFF)) {
            // 起始帧固定位不正确，判定失败
            LogUtils.e(AppConfig.MODULE_SERVER, "the begin frame is wrong,recieve fail");
            return false;
        }
        int calcDataLength = ((data[2] & 0xff) << 24)
                + ((data[3] & 0xff) << 16) + ((data[4] & 0xff) << 8)
                + (data[5] & 0xff);
        if ((dataLength - headLen - 2) != calcDataLength) {
            // 计算的长度与接收的长度进行校验，长度不正确，判定失败
            LogUtils.e(AppConfig.MODULE_SERVER, "the length verify wrong：the calcDatalength:" + calcDataLength + ";the length of realDatalength：" + (dataLength - headLen - 1));
            return false;
        }
        short crc = GetCRC_XOR_REVERSE(data, data.length - 2);
        if ((data[data.length - 1] & 0xFF) != (crc & 0xFF)||(data[dataLength-2]&0xFF)!=(crc>>8&0xFF)) {
            // CRC校验错误，判定失败
            LogUtils.e(AppConfig.MODULE_SERVER, "CRC verify error：calc CRC:" + crc + ";real CRC：" + (data[data.length - 1] & 0xFF));
            return false;
        }
        LogUtils.v(AppConfig.MODULE_SERVER, "校验通过，收到正确的包");
        return true;
    }

    /**
     * CRC校验数据（通用）
     *
     * @param data 需要检验的数据
     * @param len  校验长度
     * @return
     */
    public static short GetCRC_XOR_REVERSE(byte[] data, int len) {
//        byte crc = 0x00;
//        if (len <= data.length) {
//            for (int i = 0; i < len; i++) {
//                crc ^= data[i];
//            }
//            crc = (byte) (~crc);
//        }
        return YModem.Y_Modem_CRC(data,0,len);
    }
}
