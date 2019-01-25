package com.emptech.biocollectiononline.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.bean.FingerData;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.manager.FingerControlManager;
import com.emptech.biocollectiononline.manager.FingerManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SessionStartModeSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.futronictech.AnsiSDKLib;
import butterknife.BindView;
import pl.droidsonroids.gif.GifImageView;

import static com.futronictech.AnsiSDKLib.FTR_ANSISDK_FINGPOS_UK;


/**
 * Created by linxiaohui on 2018/1/3.
 */

public class FingerFragment extends BaseSocketFragment implements IPreview {
    private static final int MSG_SUCESS =1100;
    public static boolean[] isOpenSucced = {false, false};
    private boolean[] isStartCollection = {false, false};//是否已经开始采集；
    private boolean[] isReplay = {true, true};//指纹回复；
    private final String leftFingerFileName = "leftFingerPrint_";//保存的文件名称
    private final String rightFingerFileName = "rightFingerPrint_";//保存的文件名称


    FingerManager mFingerManager;

    @BindView(R.id.imageview_fingerprint_left)
    ImageView imageview_fingerprint_left;

    @BindView(R.id.imageview_fingerprint_right)
    ImageView imageview_fingerprint_right;

    @BindView(R.id.lay_fingerprint_left)
    RelativeLayout left_lay;
    @BindView(R.id.lay_fingerprint_right)
    RelativeLayout right_lay;

    @BindView(R.id.tv_fingerprint_left_qty)
    TextView tv_fingerprint_left_qty;
    @BindView(R.id.tv_fingerprint_right_qty)
    TextView tv_fingerprint_right_qty;
    @BindView(R.id.imageview_fingerprint_left_ok)
    ImageView left_ok;
    @BindView(R.id.imageview_fingerprint_right_ok)
    ImageView right_ok;

    @BindView(R.id.finger_left_tip)
    View finger_left_tip;
    @BindView(R.id.finger_right_tip)
    View finger_right_tip;
    @BindView(R.id.tipTv)
    TextView tip;
    @BindView(R.id.gif_view)
    GifImageView gifview;
    @BindView(R.id.finger_tv)
    TextView fingerTv;

    private final int MSG_UP_LEFT_ACTIVITY = 1000;
    private final int MSG_UP_RIGHT_ACTIVITY = 1001;
    private final int MSG_UPDATA_COLOR_LEFT = 1002;
    private final int MSG_UPDATA_COLOR_RIGHT = 1003;
    private final int MSG_UP_LEFT_COMPLETELY = 1004;
    private final int MSG_UP_RIGHT_COMPLETELY = 1005;
    private Bitmap leftFingerPrintBmp;//左手指纹图片
    private Bitmap rightFingerPrintBmp;//右手指纹图片

    private int LeftFingerPrintQty;//左手指纹质量
    private int RightFingerPrintQty;//右手指纹质量

    private int leftFingerPictureWidth;//左手指纹宽度
    private int rightFingerPictureWidth;//左手指纹宽度

    private int leftFingerPictureHeight;//左手指纹高度
    private int rightFingerPictureHeight;//左手指纹高度

    private byte[] leftFingerData;//左手数据；
    private byte[] rightFingerData;//右手数据;
    private FingerControlManager mFingerControlCallBack;
    private byte NEEDNFIQ = -1;
    private int leftOkCount;
    private int rightOkCount;
    private int initHardware=-100;
    public static final int LEFTTYPE=0x0A;
    public static final int RIGHTTYPE=0x01;
    int type;
    private boolean isFirst=true;


    @Override
    protected int getLayout() {
        return R.layout.fragment_fingerprint;
    }

    @Override
    protected void initView(View view) {
        setIPreviewListener(this);
        isOpenSucced[0]=isOpenSucced[1]=false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mSocketSession != null && (mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Right || mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Left
        ) && mSocketSession instanceof SessionStartModeSocket) {
            SessionStartModeSocket modeSocket = (SessionStartModeSocket) mSocketSession;
            NEEDNFIQ = modeSocket.getNFIQ();
            type=modeSocket.getType();
            switch (type){
                case LEFTTYPE:
//                    MusicPlayer.get(activity).playNoRepeat(R.raw.leftfinger, false);
                    right_lay.setBackgroundColor(Color.GRAY);
                    imageview_fingerprint_right.setBackgroundColor(Color.GRAY);
                    finger_right_tip.setBackgroundColor(Color.GRAY);
                    tv_fingerprint_left_qty.setTextColor(Color.WHITE);
                    gifview.setBackgroundResource(R.drawable.leftfingerprint);
                    break;
                case RIGHTTYPE:
//                    MusicPlayer.get(activity).playNoRepeat(R.raw.rightfinger, false);
                    left_lay.setBackgroundColor(Color.GRAY);
                    tv_fingerprint_right_qty.setTextColor(Color.WHITE);
                    imageview_fingerprint_left.setBackgroundColor(Color.GRAY);
                    finger_left_tip.setBackgroundColor(Color.GRAY);
                    gifview.setBackgroundResource(R.drawable.rightfingerprint);
                    break;
                default:
                    tv_fingerprint_left_qty.setTextColor(Color.WHITE);
                    tv_fingerprint_right_qty.setTextColor(Color.WHITE);
//                    MusicPlayer.get(activity).playNoRepeat(R.raw.fingerprint, false);
                    break;
            }
            isStartCollection[0] = isStartCollection[1] = false;
            imageview_fingerprint_left.setImageDrawable(null);
            imageview_fingerprint_right.setImageDrawable(null);
            openDevice();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume" );
    }

    private void openDevice() {
        if (mFingerManager == null) {
            mFingerManager = new FingerManager(App.mContext);
            mFingerManager.setOnFingerListener(rightListener, leftListener);
        }
        if (mDevice1 == null || mDevice2 == null) {
            mFingerControlCallBack = new FingerControlManager(App.mContext);
            mFingerControlCallBack.setFingerControlCallBack(mCallBack);
            boolean ret=mFingerControlCallBack.OpenUsb();
            if(!ret){
                initHardware=-1;
            }
        }
    }

    UsbDevice mDevice1, mDevice2;
    private FingerControlManager.FingerControlCallBack mCallBack = new FingerControlManager.FingerControlCallBack() {
        @Override
        public void OpenSuccessed(UsbDevice device) {
//            LogUtils.e("TAG", "收到指纹广播"+"mDevice1:"+mDevice1+"mDevice2:"+mDevice2);
            if (mDevice1 == null) {
                mDevice1 = device;
                LogUtils.e(TAG,"first finger power success" );
            } else if (mDevice2 == null) {
                mDevice2 = device;
                LogUtils.e(TAG,"both two finger power success" );
                mFingerManager.initUSBDeviceDataExchange(mDevice1, mDevice2,type);
            }
        }
    };

    @Override
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        boolean isPreviewFinger = false;
        if (mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Left) {
            LogUtils.v(TAG, "左手收集指纹");
            isPreviewFinger = true;
        } else if (mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Right) {
            LogUtils.v(TAG, "右手收集指纹");
            isPreviewFinger = true;
        }
        return isPreviewFinger;
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                if (isDetached() || isRemoving() || activity.isFinishing()) {
                    return;
                }
                switch (msg.what) {
                    case MSG_UP_LEFT_ACTIVITY:
//                        tv_fingerprint_left_qty.setText(getString(R.string.fingerprint_quality_tip) + LeftFingerPrintQty);
                        imageview_fingerprint_left.setImageBitmap(leftFingerPrintBmp);
                        break;
                    case MSG_UP_RIGHT_ACTIVITY:
//                        tv_fingerprint_right_qty.setText(getString(R.string.fingerprint_quality_tip) + RightFingerPrintQty);
                        imageview_fingerprint_right.setImageBitmap(rightFingerPrintBmp);
                        break;
                    case MSG_UPDATA_COLOR_LEFT:
                        int drawableLeftID = (Integer) msg.obj;
                        finger_left_tip.setBackgroundResource(drawableLeftID);
                        tv_fingerprint_left_qty.setTextColor(Color.BLACK);
                        left_lay.setBackgroundResource(drawableLeftID);
                        left_ok.setVisibility(View.VISIBLE);
                        break;
                    case MSG_UPDATA_COLOR_RIGHT:
                        int drawableRightID = (Integer) msg.obj;
                        finger_right_tip.setBackgroundResource(drawableRightID);
                        right_lay.setBackgroundResource(drawableRightID);
                        tv_fingerprint_right_qty.setTextColor(Color.BLACK);
                        right_ok.setVisibility(View.VISIBLE);
                        break;
                    case MSG_UP_LEFT_COMPLETELY://左手采集完成
                        updateColor(MSG_UPDATA_COLOR_LEFT, R.drawable.shape_finger_orange);
//                        tv_fingerprint_left_qty.setText(getString(R.string.fingerprint_left_tip));
                        break;
                    case MSG_UP_RIGHT_COMPLETELY://右手采集完成
                        updateColor(MSG_UPDATA_COLOR_RIGHT, R.drawable.shape_finger_orange);
//                        tv_fingerprint_right_qty.setText(getString(R.string.fingerprint_right_tip));
                        break;
                    case MSG_SUCESS:
                        tip.setVisibility(View.VISIBLE);
                        gifview.setVisibility(View.GONE);
                        fingerTv.setVisibility(View.GONE);
                        break;

                }
            }
        };
    }


    /**
     * 初始化硬件；
     *
     * @return
     */
    public boolean initHardWare() {
        long startTime = System.currentTimeMillis();
        while (!(isOpenSucced[0] && isOpenSucced[1])) {
            if (System.currentTimeMillis() - startTime > 5000) {
                LogUtils.e(TAG,"initFingerHardware timeout" );
                return false;
            }
            if(type==LEFTTYPE&&isOpenSucced[1]){
                return true;
            }else if(type==RIGHTTYPE&&isOpenSucced[0]){
                return true;
            }
            if(initHardware==-1){
                LogUtils.e(TAG,"initFingerHardware error");
                return false;
            }
            SystemClock.sleep(100);
        }
        return true;
        //TODO 未完成指纹初始化
    }


    private FingerData leftfingerDatabean;
    /**
     * 左手指纹数据监听
     */
    private FingerManager.onFingerListener leftListener = new FingerManager.onFingerListener() {

        @Override
        public void fingerPrintListener(final int width, final int height, final byte[] fingerData, final int nNFIQ, AnsiSDKLib ansiLib) {
            LogUtils.e(TAG, "leftfingerprint data:" + fingerData.length + ";width and height：[" + width + "," + height + "],quality：" + nNFIQ);
            if (isStartCollection[0]||type==RIGHTTYPE) {
                //开始采集，则停止预览
                LogUtils.v(TAG, "[左手]指纹已经开始采集，停止预览" + "isStartCollection[1]" + isStartCollection[0]
                );
                return;
            }
            LogUtils.v(TAG, "左手收集指纹数据" + fingerData.length + ";宽高：[" + width + "," + height + "],质量：" + nNFIQ);
            leftFingerPrintBmp = mFingerManager.CreateFingerBitmap(width, height, fingerData);
//            leftFingerPrintBmp = BitmapUtil.Bytes2Bimap(previewData);
            if (leftFingerPrintBmp != null) {
                LeftFingerPrintQty = nNFIQ;
                leftFingerData = fingerData;
                leftFingerPictureWidth = width;
                leftFingerPictureHeight = height;
                mWeakReferenceHandler.sendEmptyMessage(MSG_UP_LEFT_ACTIVITY);
                LogUtils.e(TAG, "NEEDNFIQ:" + NEEDNFIQ + ",nNFIQ:" + nNFIQ);
                byte[] templateData = null;
                if (NEEDNFIQ != -1 && nNFIQ <= NEEDNFIQ) {
                    leftOkCount++;
                    if (leftOkCount >= 1) {
                        byte[] collectonData = new byte[ansiLib.GetMaxTemplateSize()];
                        int[] templateSize = new int[1];
                        ansiLib.CreateTemplate(FTR_ANSISDK_FINGPOS_UK, fingerData, collectonData, templateSize);
                        templateData = new byte[templateSize[0]];
                        System.arraycopy(collectonData, 0, templateData, 0, templateSize[0]);
//                        LogUtils.e(TAG, "collectonData" + collectonData.length + "templateData:" + templateData.length + ",previewData:" + previewData.length);
                        if (templateSize[0] > 0) {
                            isStartCollection[0] = true;//开始左手采集
                            mWeakReferenceHandler.sendEmptyMessage(MSG_UP_LEFT_COMPLETELY);
                            leftOkCount = 0;
//                            FileUtil.byte2File(previewData,AppConfig.WORK_TEMP_PATH,"left.jpg" );
                            if(isStartCollection[1]||type==LEFTTYPE){
                                mWeakReferenceHandler.sendEmptyMessage(MSG_SUCESS);
                            }
//                        sendPreviewToPC(MessageType.TYPE_MODE.Finger_Left, getParmPictureData(leftFingerPrintBmp.getWidth(), leftFingerPrintBmp.getHeight(), nNFIQ, (byte) 0, previewData, templateData));
                        }else LeftFingerPrintQty=2;
                    }else LeftFingerPrintQty=2;
                } else leftOkCount = 0;
//                if(leftFingerPrintBmp!=null){
//                    leftFingerPrintBmp.setDensity(500);
//                    previewData=BitmapUtil.Bitmap2JpegBytes(leftFingerPrintBmp);
//                    FileUtil.byte2File(previewData,AppConfig.WORK_TEMP_PATH,"left.jpg" );
//                }
                byte[] previewData = BitmapUtil.compressImageToByte(leftFingerPrintBmp, 10, 6);
                leftfingerDatabean = new FingerData(leftFingerPictureWidth, leftFingerPictureHeight, (templateData != null && templateData.length > 0) ? (byte) 0 : (byte) 2, LeftFingerPrintQty, previewData, templateData);
            }

        }

    };

    /**
     * 右手指纹数据监听
     */
    private FingerData rightfingerDatabean;
    private FingerManager.onFingerListener rightListener = new FingerManager.onFingerListener() {
        @Override
        public void fingerPrintListener(final int width, final int height, final byte[] fingerData, final int nNFIQ, AnsiSDKLib ansiLib) {
            LogUtils.e(TAG, "right fingerprint data:" + fingerData.length + ";width and height：[" + width + "," + height + "],quality：" + nNFIQ);
            if (isStartCollection[1]||type==LEFTTYPE) {
                //开始采集，则停止预览
                LogUtils.v(TAG, "[右手]指纹已经开始采集，停止预览" + "isStartCollection[1]" + isStartCollection[1]
                );
                return;
            }
            rightFingerPrintBmp = mFingerManager.CreateFingerBitmap(width, height, fingerData);
//            rightFingerPrintBmp = BitmapUtil.Bytes2Bimap(previewData);
            if (rightFingerPrintBmp != null) {
                RightFingerPrintQty = nNFIQ;
                rightFingerData = fingerData;
                rightFingerPictureWidth = width;
                rightFingerPictureHeight = height;
                mWeakReferenceHandler.sendEmptyMessage(MSG_UP_RIGHT_ACTIVITY);
                byte[] templateData = null;
                if (NEEDNFIQ != -1 && nNFIQ <= NEEDNFIQ) {
                    rightOkCount++;
                    if (rightOkCount >= 1) {
                        byte[] collectonData = new byte[ansiLib.GetMaxTemplateSize()];
                        int[] templateSize = new int[1];
                        ansiLib.CreateTemplate(FTR_ANSISDK_FINGPOS_UK, fingerData, collectonData, templateSize);
                        templateData = new byte[templateSize[0]];
                        System.arraycopy(collectonData, 0, templateData, 0, templateSize[0]);
//                        LogUtils.e(TAG, "collectonData" + collectonData.length + "templateData:" + templateData.length);
                        if (templateSize[0] > 0) {
                            isStartCollection[1] = true;//开始右手采集
                            mWeakReferenceHandler.sendEmptyMessage(MSG_UP_RIGHT_COMPLETELY);
                            rightOkCount = 0;
//                            FileUtil.byte2File(previewData,AppConfig.WORK_TEMP_PATH,"right.jpg" );
                            if(isStartCollection[0]||type==RIGHTTYPE)
                                mWeakReferenceHandler.sendEmptyMessage(MSG_SUCESS);
//                            sendPreviewToPC(MessageType.TYPE_MODE.Finger_Right, getParmPictureData(rightFingerPrintBmp.getWidth(), rightFingerPrintBmp.getHeight(), nNFIQ,(byte) 0, previewData,templateData));
                        }else RightFingerPrintQty=2;
                    }else RightFingerPrintQty=2;
                } else rightOkCount = 0;
//                if(rightFingerPrintBmp!=null){
//                    rightFingerPrintBmp.setDensity(500);
//                    previewData=BitmapUtil.Bitmap2JpegBytes(rightFingerPrintBmp);
//                    FileUtil.byte2File(previewData,AppConfig.WORK_TEMP_PATH,"right.jpg" );
//                }
                byte[] previewData = BitmapUtil.compressImageToByte(rightFingerPrintBmp, 10, 6);
                rightfingerDatabean = new FingerData(rightFingerPictureWidth, rightFingerPictureHeight, (templateData != null && templateData.length > 0) ? (byte) 0 : (byte) 2, RightFingerPrintQty, previewData, templateData);
            }
        }
    };

    @Override
    public byte[] getPreviewData(SocketSession socketSession) {
        byte[] data = null;
//        LogUtils.e(TAG, "收到回复包:" + socketSession.getRunningMode());
        if (socketSession.getRunningMode() == 1) {
            if (leftfingerDatabean != null)
                data = getParmPictureData(leftfingerDatabean.getWidth(), leftfingerDatabean.getHeight(), leftfingerDatabean.getnNFIQ(), leftfingerDatabean.getStutas(), leftfingerDatabean.getPreviewData(), leftfingerDatabean.getTemplateData());
            return data;
        } else {
            if (rightfingerDatabean != null)
                data = getParmPictureData(rightfingerDatabean.getWidth(), rightfingerDatabean.getHeight(), rightfingerDatabean.getnNFIQ(), rightfingerDatabean.getStutas(), rightfingerDatabean.getPreviewData(), rightfingerDatabean.getTemplateData());
            return data;
        }
    }


    @Override
    public void closePreview() {
        super.closePreview();
        if((type==11&&!(isStartCollection[0]&&isStartCollection[1]))||(type==LEFTTYPE&&!isStartCollection[0])||(type==RIGHTTYPE&&!isStartCollection[1])){
            requestFinishActivity();
        }else {
            if(isFirst){
                stopDevice();
                isFirst=false;
            }else {
                requestFinishActivity();
            }
        }
    }

    private void stopDevice() {
        LogUtils.d(TAG, "onStop 停止采集");
        if (mFingerManager == null)
            return;
        mFingerManager.setOnFingerListener(null,null );
        mFingerManager.stopDevice();
        mFingerControlCallBack.closeFingerUsb();
        mFingerManager = null;
        mFingerControlCallBack=null;
        mDevice1 = null;
        mDevice2 = null;
    }

    /**
     * 采集模块数据
     *
     * @param runningMode
     * @return
     */
    @Override
    public byte[] collection(byte runningMode) {
        byte[] collection = null;
        int width = 0;
        int height = 0;
        int nNFIQ = 0;
        if (mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Left) {
            while (collection == null) {
                collection = leftFingerData;
            }
            width = leftFingerPictureWidth;
            height = leftFingerPictureHeight;
            nNFIQ = LeftFingerPrintQty;
//            isStartCollection[0] = true;//开始左手采集
            updateColor(MSG_UPDATA_COLOR_LEFT, R.drawable.shape_finger_orange);
        } else if (mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Finger_Right) {
            while (collection == null) {
                collection = rightFingerData;
            }
            width = rightFingerPictureWidth;
            height = rightFingerPictureHeight;
            nNFIQ = RightFingerPrintQty;
//            isStartCollection[1] = true;//开始右手采集
            updateColor(MSG_UPDATA_COLOR_RIGHT, R.drawable.shape_finger_orange);
        }
//        if(collection != null){
//            collection = BitmapUtil.Bitmap2Bytes(mFingerManager.CreateFingerBitmap(width, height, collection));
//        }
//        return getParmPictureData(width, height, nNFIQ, collection);
        return null;
    }


    /**
     * 采集结果；
     *
     * @param runningMode
     * @param isSuccess
     * @param successNumber
     */
    @Override
    public void CollectionResult(byte runningMode, boolean isSuccess, String successNumber) {
        super.CollectionResult(runningMode, isSuccess, successNumber);
//        String saveFileKey = null;//文件路径KEY
//        String saveFileValue = null;// 文件路径
//        Bitmap bitmap = null;
//        switch (runningMode) {
//            case MessageType.TYPE_MODE.Finger_Left:
//                //左手指纹采集判定
//                isReplay[0] = isSuccess;
//                isStartCollection[0] = isSuccess;
//                if (isSuccess) {
//                    //停止模块
//                    mFingerManager.stopDevice(0);
//                    // 左手指纹保存到SD卡中
//                    saveFileKey = AppConfig.PREFERENCE_KEY_LEFTFINGER;
//                    saveFileValue = leftFingerFileName + DateUtil.getCurrentTime();
//                    bitmap = leftFingerPrintBmp;
//                    updateColor(MSG_UPDATA_COLOR_LEFT, R.drawable.shape_finger_green);
//                } else {
//                    updateColor(MSG_UPDATA_COLOR_LEFT, R.drawable.shape_finger_red);
//                }
//                break;
//            case MessageType.TYPE_MODE.Finger_Right:
//                //右手指纹采集判定
//                isReplay[1] = isSuccess;
//                isStartCollection[1] = isSuccess;
//                if (isSuccess) {
//                    //停止模块
//                    mFingerManager.stopDevice(1);
//                    // 右手指纹保存到SD卡中
//                    saveFileKey = AppConfig.PREFERENCE_KEY_RIGHTFINGER;
//                    saveFileValue = rightFingerFileName + DateUtil.getCurrentTime();
//                    //路径保存到数据库
//                    bitmap = rightFingerPrintBmp;
//                    updateColor(MSG_UPDATA_COLOR_RIGHT, shape_finger_green);
//                } else {
//                    updateColor(MSG_UPDATA_COLOR_RIGHT, R.drawable.shape_finger_red);
//                }
//                break;
//        }
//        //成功，则保存数据到SD卡和数据库中；
//        if (isSuccess && saveFileValue != null && bitmap != null) {
//            File saveFile = BitmapUtil.saveBitmapToPngFile(bitmap, AppConfig.WORK_FINGER_PATH, saveFileValue);
//            //路径保存到数据库
//            String id = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
//            String pathValue = saveFile.getPath();
//            DbHelper.get(App.get()).insertPictureInfo(id, saveFileKey, pathValue, successNumber);
//        }
//        if (isReplay[0] && isReplay[1]) {
//            requestFinishActivity();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDevice();
    }

    @Override
    public void finishListener() {
        Log.e(TAG,"finishListener" );
        stopDevice();
        super.finishListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFingerManager = null;
        mFingerControlCallBack = null;
        mCallBack=null;
        leftfingerDatabean = null;
        rightfingerDatabean = null;
        rightListener=null;
        leftListener=null;
        if(leftFingerPrintBmp!=null){
            leftFingerPrintBmp.recycle();
            leftFingerPrintBmp=null;
        }
        if(rightFingerPrintBmp!=null){
            rightFingerPrintBmp.recycle();
            rightFingerPrintBmp=null;
        }
        removeIpreviewListener();
    }


    private void updateColor(int msgType, int drawableID) {
        Message message = Message.obtain();
        message.obj = drawableID;
        message.what = msgType;
        mWeakReferenceHandler.sendMessage(message);
    }
}
