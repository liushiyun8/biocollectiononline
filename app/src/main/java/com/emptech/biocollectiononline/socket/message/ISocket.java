package com.emptech.biocollectiononline.socket.message;

import com.emptech.biocollectiononline.bean.UserInfoMsg;

import org.apache.mina.core.session.IoSession;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public interface ISocket {
    public void setUserMsg(UserInfoMsg mUserInfoMsg);

    public void setRunningMode(byte Mode);

    public void setRequestID(byte[] requestID);

    public byte[] getMessageToClient(byte[] dataContent);//产生发送给客户端的包

    public void transmitIoSession(IoSession ioSession);
}
