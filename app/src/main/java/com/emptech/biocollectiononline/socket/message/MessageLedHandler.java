package com.emptech.biocollectiononline.socket.message;

import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

public class MessageLedHandler extends MessageHandler implements IMessageConfirmHandler {
    @Override
    public byte getRunningMode() {
        return runningMode;
    }

    private byte value;

    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        if (!checkByteLegitimate(data)) {
            return false;
        }
        byte[] messageContent = getMessageContentByPacket(data);
        if (( (messageContent[0] & 0xFF) != 0x00 || (messageContent[1] & 0xFF) != 0x30) ){
            return false;
        }
        runningMode=messageContent[2];
        value=messageContent[3];
        return true;
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }
}
