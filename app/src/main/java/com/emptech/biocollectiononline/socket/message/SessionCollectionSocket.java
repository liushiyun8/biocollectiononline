package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class SessionCollectionSocket extends SocketSession {
    private byte[] ID;

    @Override
    public byte[] getMessageToClient(byte[] collectionByte) {
        return createCheckPacket(ID, collectionByte);
    }


    /**
     * 采集人物ID；
     *
     * @param ID
     */
    public void setID(byte[] ID) {
        this.ID = ID;
    }

    /**
     * 生成一个采集图片校验包，发送到PC端的包
     *
     * @param ID             身份ID信息；
     * @param collectionByte 图片采集byte数据
     * @return
     */
    private byte[] createCheckPacket(byte[] ID, byte[] collectionByte) {
        if (ID == null || runningMode == 0x00) {
            LogUtils.e(AppConfig.MODULE_SERVER, "collection packet 4001 error，can't makepacket；");
            return null;
        }
        byte[] messageType = MessageType.ANDROID_TYPE.TYPE_START_ACTION_COLLECTION;
        //包体长度： messagetype+ mode+IDLen+ID+collectionLen+collection

        byte[] dataContent;
        if(collectionByte==null){
           dataContent=new byte[1+messageType.length+1];
           int index=0;
           dataContent[index]=0x01;
           index++;
           System.arraycopy(messageType, 0, dataContent, index, messageType.length);
           index+=messageType.length;
           dataContent[index]=runningMode;
           return makePacket(dataContent);
        }
        int collectionLen = collectionByte.length;
        dataContent = new byte[1+messageType.length + 1 + 1 + ID.length + 4 + collectionLen];
        int index = 0;
        //类型MessageType
        dataContent[index]=0x00;
        index++;
        System.arraycopy(messageType, 0, dataContent, index, messageType.length);
        index += messageType.length;
        //MODE
        dataContent[index] = runningMode;
        index++;
        //ID
        dataContent[index] = (byte) (ID.length & 0xFF);
        index++;
        System.arraycopy(ID, 0, dataContent, index, ID.length);
        index += ID.length;
        //Collection
        // 长度= 包体长度
        dataContent[index] = (byte) (((collectionLen) >> 24) & 0xFF);
        index++;
        dataContent[index] = (byte) (((collectionLen) >> 16) & 0xFF);
        index++;
        dataContent[index] = (byte) (((collectionLen) >> 8) & 0xFF);
        index++;
        dataContent[index] = (byte) ((collectionLen) & 0xFF);
        index++;
        System.arraycopy(collectionByte, 0, dataContent, index, collectionByte.length);
        return makePacket(dataContent);
    }
}
