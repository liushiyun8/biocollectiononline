package com.emptech.biocollectiononline.socket.message;

public class MesssageCloseHandler extends MessageHandler implements IMessageConfirmHandler {


    @Override
    public byte getRunningMode() {
        return runningMode;
    }

    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        byte[] content = getMessageContentByPacket(data);
        if(content==null)
            return null;
        if(content[0]!=0x70||content[1]!=0x00){
            return null;
        }
        if(content.length>=3)
        runningMode = content[2];
        return true;
    }
}
