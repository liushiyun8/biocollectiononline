package com.emptech.biocollectiononline.socket.message;


import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * 确认使用采集资源报文格式,人像采集确认使用，5000包
 */

public class MessageConfirmHandler extends MessageHandler implements IMessageConfirmHandler {

    private boolean isConfirm = false;

    @Override
    public byte getRunningMode() {
        return runningMode;
    }


    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        if (!checkByteLegitimate(data)) {
            return false;
        }
        byte[] messageContent = getMessageContentByPacket(data);
        int index = 0;
        if ((messageContent[index] & 0xFF) != 0x00 || (messageContent[index + 1] & 0xFF) != 0x50 || (messageContent[index + 2] & 0xFF) != 0x01) {
            return false;
        }
        index += 3;
        runningMode = messageContent[index];
        index++;
        if ((messageContent[index] & 0xFF) == 0x00) {
            isConfirm = true;
        }
        return true;
    }

    public boolean isConfirmSuccess() {
        return isConfirm;
    }

}
