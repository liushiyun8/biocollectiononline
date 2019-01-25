package com.emptech.biocollectiononline.socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.activity.ModeFingerPrintActivity;
import com.emptech.biocollectiononline.activity.ModePhotoActivity;
import com.emptech.biocollectiononline.activity.ModeSigntureActivity;
import com.emptech.biocollectiononline.activity.UserInfoActivity;
import com.emptech.biocollectiononline.activity.UserInfoConfirmActivity;
import com.emptech.biocollectiononline.bean.EventMsg;
import com.emptech.biocollectiononline.bean.UserInfoMsg;
import com.emptech.biocollectiononline.bean.VersionBean;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.LedManager;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.SocketSessionManager;
import com.emptech.biocollectiononline.manager.VersionManager;
import com.emptech.biocollectiononline.socket.message.IMessageConfirmHandler;
import com.emptech.biocollectiononline.socket.message.IMessageUserInfo;
import com.emptech.biocollectiononline.socket.message.MessageCollectionCheckHandler;
import com.emptech.biocollectiononline.socket.message.MessageCollectionHandler;
import com.emptech.biocollectiononline.socket.message.MessageConfirmHandler;
import com.emptech.biocollectiononline.socket.message.MessageHeartHandler;
import com.emptech.biocollectiononline.socket.message.MessageLedHandler;
import com.emptech.biocollectiononline.socket.message.MessagePreviewHandler;
import com.emptech.biocollectiononline.socket.message.MessageSigntureCheckHandler;
import com.emptech.biocollectiononline.socket.message.MessageStartModeHandler;
import com.emptech.biocollectiononline.socket.message.MessageUserAllInfoHandler;
import com.emptech.biocollectiononline.socket.message.MessageUserInfoHandler;
import com.emptech.biocollectiononline.socket.message.MessageVersionHandler;
import com.emptech.biocollectiononline.socket.message.MesssageCloseHandler;
import com.emptech.biocollectiononline.socket.message.SessionCloseSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionCollectionSocket;
import com.emptech.biocollectiononline.socket.message.SessionConfirmSocket;
import com.emptech.biocollectiononline.socket.message.SessionHeartSocket;
import com.emptech.biocollectiononline.socket.message.SessionLedSocket;
import com.emptech.biocollectiononline.socket.message.SessionPreviewSocket;
import com.emptech.biocollectiononline.socket.message.SessionSigntureCheckSocket;
import com.emptech.biocollectiononline.socket.message.SessionStartModeSocket;
import com.emptech.biocollectiononline.socket.message.SessionUserAllInfoSocket;
import com.emptech.biocollectiononline.socket.message.SessionUserInfoSocket;
import com.emptech.biocollectiononline.socket.message.SessionVersionSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.CommUtils;
import com.emptech.biocollectiononline.utils.Converter;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.google.gson.Gson;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import cn.xiongdi.jni.UserDevices;

import static com.emptech.biocollectiononline.fragment.UserInfoFragment.IDKey;


/**
 * 通讯服务核心处理类；
 */
public class SocketServerHandler extends IoHandlerAdapter {
    private Context mContext;
    Handler handler;
    private MessagePreviewHandler MessagePreviewHandler;
    private SessionPreviewSocket sessionPreviewSocket;
    private long id;

    public SocketServerHandler(Context context) {
        mContext = context;
    }

    public SocketServerHandler(Context context, Handler handler) {
        mContext = context;
        this.handler=handler;
    }


    @Override
    public void exceptionCaught(IoSession session, Throwable cause){
        cause.printStackTrace();
        //网络灯亮红灯
        UserDevices.internet_status((byte) 0);
        LogUtils.e(AppConfig.MODULE_SERVER, "exceptionCaught,sessionId：" + session.getId() + ";the message of cause：" + cause.getMessage());
        if (session.isConnected()) {
            session.closeNow();
            LogUtils.e(AppConfig.MODULE_SERVER, "exceptionCaught，close all activities and all session！");
            App.get().finishAllActivitys();
//            session.closeNow();
        }
    }

    /**
     * 接受消息
     */

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        IoBuffer buf = (IoBuffer) message;
        int len = buf.limit();
        byte[] bufByte = new byte[len];
        buf.get(bufByte);
        buf.free();
        if(AppConfig.isTestSocket)
        LogUtils.e(AppConfig.MODULE_SERVER, "messageReceived..." + Converter.BytesToHexString(bufByte, bufByte.length>30?30:bufByte.length));
//        String fn=new SimpleDateFormat("yyyy_MM_dd@HH_mm_ss").format(new Date())+"#2.bin";
//        FileUtil.byte2File(bufByte,SDCardUtil.getSDCardPath()+"/bin2", fn);
        decodeBuf(session, bufByte);
    }


    /**
     * 发送消息
     */
    @Override
    public void messageSent(IoSession session, Object message){
        byte[] messageByte = (byte[]) message;
        if(AppConfig.isTestSocket)
        LogUtils.e(AppConfig.MODULE_SERVER, "send to " + session.getRemoteAddress() + ";send length：" + messageByte.length);
        LogUtils.v(AppConfig.MODULE_SERVER, "send data:" + Converter.BytesToHexString(messageByte, messageByte.length>50?50:messageByte.length));
    }

    @Override
    public void sessionClosed(IoSession session){
        LogUtils.e(AppConfig.MODULE_SERVER, "sessionClosed");
        //网络灯亮红灯
        if(id==session.getId()){
            UserDevices.internet_status((byte) 0);
            EventMsg.setCurrentNetStatus(EventMsg.EVENT_NET_LOST);
            EventBus.getDefault().post(new EventMsg(EventMsg.EVENT_MSG_NETCHANGED, EventMsg.EVENT_NET_LOST, 0, null));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(App.getAppContext(), R.string.connection_close ,Toast.LENGTH_LONG ).show();
                }
            });
            session.closeNow();
            App.get().finishAllActivitys();
        }
    }

    /**
     * 1.创建session
     */
    @Override
    public void sessionCreated(IoSession session){
        id = session.getId();
        Map<Long, IoSession> managedSessions = session.getService().getManagedSessions();
        Log.v(AppConfig.MODULE_SERVER,"getManagedSessions"+managedSessions.size() );
        if(managedSessions.size()>1){
            for (Map.Entry<Long, IoSession> entry : managedSessions.entrySet()) {
                if(id!=entry.getValue().getId()){
                    if(entry.getValue().isConnected())
                    entry.getValue().closeNow();
                }
            }
        }
        EventMsg.setCurrentNetStatus(EventMsg.EVENT_NET_WAITING);
        EventBus.getDefault().post(new EventMsg(EventMsg.EVENT_MSG_NETCHANGED, EventMsg.EVENT_NET_WAITING, 0, null));
        LogUtils.e(AppConfig.MODULE_SERVER, session.getLocalAddress() + " and:" + session.getRemoteAddress() + " connected");
    }

    /**
     * 空闲状态 服务器检测客户端是否断线，心跳
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        LogUtils.v(AppConfig.MODULE_SERVER, "sessionIdle");
    }

    /**
     * 2.session开启
     */
    @Override
    public void sessionOpened(IoSession session){
        LogUtils.e(AppConfig.MODULE_SERVER, "sessionOpened");
        //网络灯亮绿灯
        UserDevices.internet_status((byte) 1);
        EventMsg.setCurrentNetStatus(EventMsg.EVENT_NET_CONNECT);
        EventBus.getDefault().post(new EventMsg(EventMsg.EVENT_MSG_NETCHANGED, EventMsg.EVENT_NET_CONNECT, 0, null));
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.getAppContext(), R.string.connection_open ,Toast.LENGTH_LONG ).show();
            }
        });
        App.get().finishAllActivitys();
    }


    private void decodeBuf(IoSession session, byte[] bufByte) {
        MessageType.TYPE_MESSAGE type = MessageUtils.getTypeByRecData(bufByte);
        IMessageConfirmHandler imessageConfirm = null;
        IMessageUserInfo imessageUser = null;
        Class startActivity = null;
        SocketSession iSocket = null;
        switch (type) {
            case PHOTO:
                LogUtils.e(AppConfig.MODULE_SERVER, "collect photo...");
                imessageConfirm = new MessageConfirmHandler();
                iSocket = new SessionConfirmSocket();
                break;
            case USER:
                LogUtils.e(AppConfig.MODULE_SERVER, "recieve user information...");
                imessageUser = new MessageUserInfoHandler();
                iSocket = new SessionUserInfoSocket();
                startActivity = UserInfoActivity.class;
                break;
            case START_ACTION_MODE:
                LogUtils.e(AppConfig.MODULE_SERVER, "start model mode...");
                imessageConfirm = new MessageStartModeHandler();
                iSocket = new SessionStartModeSocket();
                imessageConfirm.handlerMessageFromServer(bufByte);
                switch (imessageConfirm.getRunningMode()) {
                    case MessageType.TYPE_MODE.Finger_Left:
                    case MessageType.TYPE_MODE.Finger_Right:
                        startActivity = ModeFingerPrintActivity.class;
                        break;
                    case MessageType.TYPE_MODE.Photo:
                        startActivity = ModePhotoActivity.class;
                        break;
                    case MessageType.TYPE_MODE.Signture:
                        startActivity = ModeSigntureActivity.class;
                        break;
                }
                break;
            case START_ACTION_COLLECTION:
                LogUtils.e(AppConfig.MODULE_SERVER, "start collection...");
                imessageConfirm = new MessageCollectionHandler();
                iSocket = new SessionCollectionSocket();
                break;
            case PREVIEW:
                if(AppConfig.isTestSocket)
                LogUtils.e(AppConfig.MODULE_SERVER, "start preview...");
                if(MessagePreviewHandler==null){
                    MessagePreviewHandler = new MessagePreviewHandler();
                }
                imessageConfirm = this.MessagePreviewHandler;
                if(sessionPreviewSocket==null)
                sessionPreviewSocket = new SessionPreviewSocket();
                iSocket = sessionPreviewSocket;
                break;
            case CHECK_COLLECTION:
                LogUtils.e(AppConfig.MODULE_SERVER, "collection check...");
                imessageConfirm = new MessageCollectionCheckHandler();
                iSocket = new SessionCollectionCheckSocket();
                break;
            case START_CHECK_COLLECTION:
                LogUtils.e(AppConfig.MODULE_SERVER, "start check collection...");
                imessageConfirm = new MessageSigntureCheckHandler();
                imessageConfirm.handlerMessageFromServer(bufByte);
                iSocket = new SessionSigntureCheckSocket();
                ((SessionSigntureCheckSocket) iSocket).setRecPhotoNumber(((MessageSigntureCheckHandler) imessageConfirm).getRecPhotoNumber());
                ((SessionSigntureCheckSocket) iSocket).setUserNumber(((MessageSigntureCheckHandler) imessageConfirm).getUseID());
                ((SessionSigntureCheckSocket) iSocket).setSingturePictureData(((MessageSigntureCheckHandler) imessageConfirm).getPicture());
                break;
            case ALL_INFO:
                LogUtils.e(AppConfig.MODULE_SERVER, "show all inforfation of user...");
                imessageUser=new MessageUserAllInfoHandler();
                iSocket=new SessionUserAllInfoSocket();
                startActivity = UserInfoConfirmActivity.class;
                break;
            case CLOSE_VIDEO:
                LogUtils.e(AppConfig.MODULE_SERVER, "close modle...");
                imessageConfirm=new MesssageCloseHandler();
                iSocket=new SessionCloseSocket();
                break;
            case HEART:
                LogUtils.e(AppConfig.MODULE_SERVER, "heart packet...");
                imessageConfirm=new MessageHeartHandler();
                iSocket=new SessionHeartSocket();
                break;
            case GETVERSION:
                LogUtils.e(AppConfig.MODULE_SERVER, "get version...");
                imessageConfirm=new MessageVersionHandler();
                iSocket=new SessionVersionSocket();
                break;
            case LED:
                LogUtils.e(AppConfig.MODULE_SERVER, "adjust led...");
                imessageConfirm=new MessageLedHandler();
                iSocket=new SessionLedSocket();
                break;
            default:
                LogUtils.e(AppConfig.MODULE_SERVER, "recieve error message...");
                break;
        }
        if (iSocket != null) {
            iSocket.transmitIoSession(session);
            if (imessageConfirm != null) {
                boolean isSuccess = imessageConfirm.handlerMessageFromServer(bufByte);
                if (isSuccess) {
                    if (imessageConfirm instanceof MessageCollectionCheckHandler) {
                        //设置图片编号
                        String messageNumber = ((MessageCollectionCheckHandler) imessageConfirm).getRecPhotoNumber();
                        ((SessionCollectionCheckSocket) iSocket).setRecPhotoNumber(messageNumber);
                        ((SessionCollectionCheckSocket) iSocket).setCheckSuccess(((MessageCollectionCheckHandler) imessageConfirm).isCheckSuccess()
                        );
                    } else if (imessageConfirm instanceof MessageConfirmHandler) {
                        //人像采集确认包；
                        boolean isConfirmSuccess = ((MessageConfirmHandler) imessageConfirm).isConfirmSuccess();
                        ((SessionConfirmSocket) iSocket).setSuccess(isConfirmSuccess);
                    }else if(imessageConfirm instanceof MessageStartModeHandler) {
                        MessageStartModeHandler modeHandler = (MessageStartModeHandler) imessageConfirm;
                        int previewWidth = (modeHandler).getPreviewWidth();
                        int previewHeight = modeHandler.getPreviewHeight();
                        ((SessionStartModeSocket) iSocket).setPreviewWidth(previewWidth);
                        ((SessionStartModeSocket) iSocket).setPreviewHeight(previewHeight);
                        ((SessionStartModeSocket) iSocket).setNFIQ(modeHandler.getNFIQ());
//                        ((SessionStartModeSocket) iSocket).setOvertime(modeHandler.getOvertime());
                        ((SessionStartModeSocket) iSocket).setType(modeHandler.getType());
                         iSocket.setRunningMode(modeHandler.getRunningMode());
                    }
                    else if(imessageConfirm instanceof MesssageCloseHandler){
                        iSocket.setRequestID(imessageConfirm.getRequestID());
                        iSocket.setRunningMode(imessageConfirm.getRunningMode());
                        if(AppConfig.isTest){
                            if(AppConfig.TestError){
                                session.write(iSocket.getMessageToClient(new byte[]{0x01}));
                            }
                            if(AppConfig.TestClose){

                            }
                        }else
                        session.write(iSocket.getMessageToClient(new byte[]{0x00}));
                    }else if(imessageConfirm instanceof MessageHeartHandler){
//                        LogUtils.e("MessageHeartHandler", "getRunningMode"+MessagePreviewHandler.getRunningMode());
                        iSocket.setRequestID(imessageConfirm.getRequestID());
                        iSocket.setRunningMode(imessageConfirm.getRunningMode());
                        byte[] bytes = {0x00};
                        if(AppConfig.isTest){
                            if(AppConfig.TestTimeout)
                                return;
                            if(AppConfig.TestError){
                                bytes[0]=0x01;
                            }
                        }
                        session.write(iSocket.getMessageToClient(bytes));
                        return;
                    }else if(imessageConfirm instanceof MessageVersionHandler){
                        iSocket.setRequestID(imessageConfirm.getRequestID());
                        iSocket.setRunningMode(imessageConfirm.getRunningMode());
                        byte[] data=getVersionJson();
                        if(AppConfig.isTest){
                            if(AppConfig.TestError){
                                data=null;
                            }
                            if(AppConfig.TestTimeout){
                                return;
                            }
                        }
                        session.write(iSocket.getMessageToClient(data));
                        return;
                    }else if(imessageConfirm instanceof MessageLedHandler){
                        iSocket.setRequestID(imessageConfirm.getRequestID());
                        iSocket.setRunningMode(imessageConfirm.getRunningMode());
                        byte value = ((MessageLedHandler) imessageConfirm).getValue();
                        int ret=LedManager.open(255*value/100);
                        byte[] bytes = {0x00};
                        if(ret!=0){
                            bytes[0]=0x01;
                        }else {
                            LedManager.saveCurrentValue(255*value/100);
                        }
                        session.write(iSocket.getMessageToClient(bytes));
                        return;
                    }
                    iSocket.setRequestID(imessageConfirm.getRequestID());
                    iSocket.setRunningMode(imessageConfirm.getRunningMode());
                    updateActivity(iSocket, startActivity, bufByte);
                }
            } else if (imessageUser != null) {
                UserInfoMsg userMsg = imessageUser.handlerMessageFromServer(bufByte);
                Map<String,String>  map=new HashMap<>();
                map.put("ID","100");
                userMsg.setmUserInfo(map);
                LogUtils.e(AppConfig.MODULE_SERVER,"UserInfoMsg:"+ userMsg.getmUserInfo());
                if(imessageUser instanceof MessageUserInfoHandler) {
                    if (userMsg != null) {
                        //接收到USER信息包，销毁所有界面；
                        App.get().finishAllActivitys();
                        iSocket.setRequestID(imessageUser.getRequestID());
                        iSocket.setUserMsg(userMsg);
                        byte[] success = {0x00};
                        boolean isSend = false;
                        if (session.isConnected()) {
                            session.write(iSocket.getMessageToClient(success));
                            isSend = true;
                        }
                        if (isSend) {
                            saveMessage2Db(userMsg);
                        }
//                    updateActivity(iSocket, startActivity, bufByte);
                    }
                }else if(imessageUser instanceof MessageUserAllInfoHandler){
                    if(userMsg!=null){
                        updateMessage2Db(userMsg);
                    }
                    iSocket.setRequestID(imessageUser.getRequestID());
                    iSocket.setUserMsg(userMsg);
                    iSocket.setRunningMode(((MessageUserAllInfoHandler)imessageUser).getRunningMode());
                    byte[] success = {0x00};
                    if(AppConfig.isTest){
                        if(AppConfig.TestError){
                           success[0]=0x01;
                        }
                        if (session.isConnected()&&!AppConfig.TestTimeout) {
                            session.write(iSocket.getMessageToClient(success));
                        }
                    }else {
                        if (session.isConnected()) {
                            session.write(iSocket.getMessageToClient(success));
                        }
                    }
                    updateActivity(iSocket,startActivity,bufByte);
                }
            }
        }
    }

    private byte[] getVersionJson() {
        VersionBean versionBean = new VersionBean();
        versionBean.setAppVersion(CommUtils.getAppVersion(mContext));
        String display = Build.DISPLAY;
        versionBean.setSysVersion(display.substring(display.indexOf("EMP"),display.length()));
        String powerV=VersionManager.getPmVersion();
        versionBean.setPowerVersion(powerV);
        versionBean.setServerVersion(VersionManager.getServerVersion(mContext));
        versionBean.setHardTestVersion(VersionManager.getHardVersion(mContext));
        versionBean.setSignatureVersion(VersionManager.getSignatureVersion());
        String json = new Gson().toJson(versionBean);
        Log.v(AppConfig.MODULE_SERVER, "json:"+json);
        return json.getBytes();
    }

    private void saveMessage2Db(UserInfoMsg userMsg) {
        Map<String, String> userInfo = userMsg.getmUserInfo();
        String UserID = userInfo.get(IDKey);
        LogUtils.v(AppConfig.MODULE_SERVER, "保存ID：" + UserID);
        userInfo.remove(IDKey);
        PreferencesManager.getIns(App.get()).setStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER, UserID);
        DbHelper.get(App.get()).createIDUserInfo(UserID, userInfo);
    }

    private void updateMessage2Db(UserInfoMsg userMsg) {
        Map<String, String> userInfo = userMsg.getmUserInfo();
        String UserID = userInfo.get(IDKey);
        LogUtils.v(AppConfig.MODULE_SERVER, "保存ID：" + UserID);
        userInfo.remove(IDKey);
        PreferencesManager.getIns(App.get()).setStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER, UserID);
        DbHelper.get(App.get()).updateIDUserInfo(UserID, userInfo);
    }



    private void updateActivity(final SocketSession iSocket, final Class<? extends Activity> startActivity, byte[] data) {
        if (startActivity != null &&(!App.get().hasthisActivity(startActivity)||!startActivity.getSimpleName().equals(ModePhotoActivity.class.getSimpleName())) ) {
            //启动Activity
            if(AppConfig.isTestSocket)
            LogUtils.e(AppConfig.MODULE_SERVER, "start activity:" + startActivity.getSimpleName());
            startActivity(startActivity, iSocket);
        } else {
            if(AppConfig.isTestSocket)
            LogUtils.e(AppConfig.MODULE_SERVER, "post data to Activity....");
            EventBus.getDefault().post(iSocket);
        }
    }


    protected void startActivity(final Class<? extends Activity> clazz, final SocketSession iSocket) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                App.get().finishAllActivitys();
                SocketSessionManager.getInstance().putSocketSession(clazz, iSocket);
                Intent intent = new Intent(App.get(), clazz);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        });
    }

    protected void startActivity(Class<? extends Activity> clazz){
        Intent intent = new Intent(App.get(), clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }
}
