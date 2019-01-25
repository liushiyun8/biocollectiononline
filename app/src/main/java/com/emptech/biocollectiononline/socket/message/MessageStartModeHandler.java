package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.utils.LogUtils;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.emptech.biocollectiononline.socket.MessageUtils.checkByteLegitimate;

/**
 * Created by linxiaohui on 2018/1/2.
 */

public class MessageStartModeHandler extends MessageHandler implements IMessageConfirmHandler {


    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    private byte type;

    @Override
    public byte getRunningMode() {
        return runningMode;
    }

    public int previewWidth;
    public int PreviewHeight;
    public byte NFIQ;

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return PreviewHeight;
    }

    public void setPreviewHeight(int getPreviewHeight) {
        this.PreviewHeight = getPreviewHeight;
    }

    public byte getNFIQ() {
        return NFIQ;
    }

    public void setNFIQ(byte NFIQ) {
        this.NFIQ = NFIQ;
    }


    //    public static void main(String[] args) {
//        System.out.println("Hello World.");
//        byte[] dataContent={2,3,4,5,1,-112,-112,1,2,3};
//        ByteBuffer buffer= ByteBuffer.wrap(dataContent);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        buffer.position(3);
//        short aShort = buffer.getShort();
//        short bShort = buffer.getShort();
//        System.out.println("ashort:"+aShort+",bshort:"+bShort);
//    }

    @Override
    public Boolean handlerMessageFromServer(byte[] data) {
        if (!checkByteLegitimate(data)) {
            return false;
        }
        byte[] dataContent = getMessageContentByPacket(data);
        if ((dataContent[0] & 0xFF) != 0x20 || (dataContent[1] & 0xFF) != 0x00) {
            return false;
        }
        runningMode = dataContent[2];
        if(runningMode== MessageType.TYPE_MODE.Photo&&dataContent.length>3){
            LogUtils.e(AppConfig.MODULE_SERVER,"3:"+dataContent[3]+"4："+dataContent[4] );
            LogUtils.e(AppConfig.MODULE_SERVER,"5:"+dataContent[5]+"6："+dataContent[6] );
            ByteBuffer buffer= ByteBuffer.wrap(dataContent);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(3);
            previewWidth=buffer.getShort();
            PreviewHeight=buffer.getShort();
        }else if(runningMode== MessageType.TYPE_MODE.Finger_Left||runningMode== MessageType.TYPE_MODE.Finger_Right&&dataContent.length>3){
           try {
               NFIQ=dataContent[3];
               type=dataContent[4];
               LogUtils.e(AppConfig.MODULE_SERVER, "NFIQ:"+NFIQ);
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        return true;
    }
}
