package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.apache.mina.core.session.IoSession;

import java.io.Serializable;
import java.util.Arrays;

import static com.emptech.biocollectiononline.socket.MessageConfig.ReserveFlagLen;
import static com.emptech.biocollectiononline.socket.MessageConfig.STARTFRAME;
import static com.emptech.biocollectiononline.socket.MessageConfig.headLen;
import static com.emptech.biocollectiononline.socket.MessageUtils.GetCRC_XOR_REVERSE;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public abstract class SocketSession implements ISocket, Serializable {
    private static final long serialVersionUID = 7060210544600464481L;
    private byte[] requestID;
    private IoSession mIoSession;
    protected byte runningMode;

    @Override
    public void setUserMsg(UserInfoMsg mUserInfoMsg) {
    }

    @Override
    public void setRunningMode(byte Mode) {
        runningMode = Mode;
    }

    public byte getRunningMode() {
        return runningMode;
    }

    @Override
    public void setRequestID(byte[] requestID) {
        this.requestID = requestID;
    }


    public byte[] getRequestID() {
        return requestID;
    }

    @Override
    public void transmitIoSession(IoSession ioSession) {
        mIoSession = ioSession;
    }

    /**
     * 获得发送消息到PC端的session
     *
     * @return
     */
    public IoSession getmIoSession() {
        return mIoSession;
    }

    /**
     * 组包时候获取包头（通用）
     *
     * @param dataLen   包体长度
     * @param RequestID 由客户端定义的用于匹配请求消息的ID号
     */
    private byte[] getPacketHead(int dataLen, byte[] RequestID) {
        if (RequestID == null || RequestID.length != 4) {
            LogUtils.e(AppConfig.MODULE_SERVER, "get packethead ,RequestID verify error");
            return null;
        }
        byte[] packetHead = new byte[headLen];// 包头长度
        int index = 0;
        packetHead[index] = STARTFRAME[0];
        index++;
        packetHead[index] = STARTFRAME[1];
        index++;
        // 长度= 包体长度
        packetHead[index] = (byte) (((dataLen) >> 24) & 0xFF);
        index++;
        packetHead[index] = (byte) (((dataLen) >> 16) & 0xFF);
        index++;
        packetHead[index] = (byte) (((dataLen) >> 8) & 0xFF);
        index++;
        packetHead[index] = (byte) ((dataLen) & 0xFF);
        index++;
        // 请求ID
        System.arraycopy(RequestID, 0, packetHead, index, RequestID.length);
        index += RequestID.length;
        //预留8字节
        Arrays.fill(packetHead, index, index + ReserveFlagLen, (byte) 0xFF);
        index += ReserveFlagLen;
        return packetHead;
    }

    /**
     * 组成一个完整分包帧数据（通用）
     *
     * @param data 分包数据
     * @return 当前包数据
     */
    protected byte[] makePacket(byte[] data) {
        //组合包头
        if (requestID == null) {
            LogUtils.e(AppConfig.MODULE_SERVER, "requestID can't get ，can't make packet");
            return null;
        }
        byte[] head = getPacketHead(data.length, requestID);
        if (head == null) {
            LogUtils.e(AppConfig.MODULE_SERVER, "packetHead error，stop make packet");
            return null;
        }
        // 完整帧数据=包头+包体+CRC校验位
        int len = head.length + data.length + 2;
        byte[] packet = new byte[len];
        // 包头加入到帧中
        System.arraycopy(head, 0, packet, 0, head.length);
        // 包体加入到帧中
        System.arraycopy(data, 0, packet, head.length, data.length);
        short CRC = GetCRC_XOR_REVERSE(packet, packet.length - 2);
        packet[len - 1] = (byte) (CRC&0xFF);
        packet[len - 2] = (byte) (CRC>>8&0xFF);
//        packet[len - 1] = (byte) (0x22);
////        packet[len - 2] = (byte) (0x12);
        return packet;
    }

}
