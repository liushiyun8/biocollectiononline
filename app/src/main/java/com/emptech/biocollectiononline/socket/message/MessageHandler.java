package com.emptech.biocollectiononline.socket.message;

import static com.emptech.biocollectiononline.socket.MessageConfig.headLen;
import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * 组合包体类；
 */

public abstract class MessageHandler {

    protected byte runningMode;

    private byte[] requestID;


    public byte[] getRequestID() {
        return requestID;
    }


    /**
     * 获取包体内容
     *
     * @param data
     * @return
     */
    public byte[] getMessageContentByPacket(byte[] data) {
        if (!checkByteLegitimate(data)) {
            return null;
        }
        int calcDataLength = ((data[2] & 0xff) << 24)
                + ((data[3] & 0xff) << 16) + ((data[4] & 0xff) << 8)
                + (data[5] & 0xff);
        byte[] reData = new byte[calcDataLength];
        System.arraycopy(data, headLen, reData, 0, calcDataLength);
        byte[] requestID = new byte[4];
        System.arraycopy(data, 6, requestID, 0, requestID.length);
        this.requestID = requestID;
        return reData;
    }
}
