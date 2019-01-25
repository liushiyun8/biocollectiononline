package com.emptech.biocollectiononline.socket.message;

/**
 * Created by linxiaohui on 2018/1/5.
 */

public interface IPreview {
    public boolean initHardWare();//初始化预览硬件

    public byte[] getPreviewData(SocketSession socketSession);//获取预览数据

    public byte[] collection(byte runningMode);//获取模块的采集数据；
}
