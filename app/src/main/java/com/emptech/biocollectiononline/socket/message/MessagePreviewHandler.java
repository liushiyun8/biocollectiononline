package com.emptech.biocollectiononline.socket.message;

import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * Created by linxiaohui on 2018/1/2.
 */

public class MessagePreviewHandler extends MessageHandler implements  IMessageConfirmHandler{


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
        if (( (messageContent[0] & 0xFF) != 0x30 || (messageContent[1] & 0xFF) != 0x01) ){
            return false;
        }
        runningMode=messageContent[2];
        return true;
    }


}
