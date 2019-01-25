package com.emptech.biocollectiononline.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.SignatureManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SessionSigntureCheckSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.DateUtil;
import com.emptech.biocollectiononline.utils.FileUtil;
import com.emptech.biocollectiononline.utils.LogUtils;
import java.io.File;

import butterknife.BindView;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by linxiaohui on 2018/1/3.
 */

public class SigntureFragment extends BaseSocketFragment implements IPreview {
    @BindView(R.id.show_message_tv)
    TextView show_message_tv;
    @BindView(R.id.showsignature_iv)
    ImageView showsignature_iv;
    @BindView(R.id.gifView)
    GifImageView gifImageView;

    private SignatureManager mSignatureManager;
    private Bitmap signatureBmp;// 签字图片

    private final static int MSG_SHOW_BMP = 1001;// 刷新图片
    private final static int MSG_SHOW_MESSAGE = 1002;// 显示信息
    private boolean isOpen;
    private Thread thread;
    private boolean isStart;
    private int initHardware=-100;
    private byte[] PictureData;
    private boolean isTestSig=true;

    @Override
    protected int getLayout() {
        return R.layout.fragment_signture;
    }

    private void showMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_SHOW_MESSAGE;
        msg.obj = message;
        mWeakReferenceHandler.sendMessage(msg);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.v(TAG, "onCreate~~~~~~~~~~~~~~~~~~~~~~~~~~`");
    }

    @Override
    public void onDestroyView() {
        release();
        super.onDestroyView();
        LogUtils.v(TAG, "onDestroyView~~~~~~~~~~~~~~~~~~~~~~~~~~`");
    }

    private void release() {
        mSignatureManager.CloseCapture();
        try {
            if(thread!=null)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if(signatureBmp!=null){
//            signatureBmp.recycle();
//            signatureBmp=null;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.v(TAG, "onDestroy~~~~~~~~~~~~~~~~~~~~~~~~~~`");

    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.v(TAG, "onStop~~~~~~~~~~~~~~~~~~~~~~~~~~`");

    }

    @Override
    public void onResume() {
        super.onResume();
//        showsignature_iv.setImageDrawable(null);
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        WeakReferenceHandler.MyHandleMessage myHandleMessage = new WeakReferenceHandler.MyHandleMessage() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SHOW_BMP:
                        if (showsignature_iv != null && signatureBmp != null) {
                            showsignature_iv.setImageBitmap(signatureBmp);
//                            signatureBmp.setDensity(500);
                            byte[] previewData=BitmapUtil.Bitmap2JpegBytes(signatureBmp);
                            FileUtil.byte2File(previewData, AppConfig.WORK_TEMP_PATH, "signature.jpg");
                            PictureData = getParmPictureData(signatureBmp.getWidth(), signatureBmp.getHeight(), 1, previewData);
                            show_message_tv.setVisibility(View.GONE);
                            gifImageView.setVisibility(View.GONE);
//                            SigntureFinish=true;
//                            if(isTestSig){
//                                release();
//                                Log.e(TAG,"释放成功" );
//                                openDevice();
//                                Log.e(TAG,"打开成功" );
//                            }
                        }

                        break;
                    case MSG_SHOW_MESSAGE:
                        if (show_message_tv != null && msg.obj != null)
//                        show_message_tv.setText((String) msg.obj);
                            break;
                    default:
                        break;
                }
            }
        };
        return myHandleMessage;
    }

    @Override
    public void closePreview() {
        super.closePreview();
        if(signatureBmp==null){
            requestFinishActivity();
        }else {
            signatureBmp.recycle();
            signatureBmp=null;
        }
    }

    @Override
    protected void startCollection(SocketSession mSocketSession) {
        sendCollectionResult(mSocketSession, PictureData);
    }

    @Override
    protected void initView(View view) {
        setIPreviewListener(this);
        mSignatureManager = new SignatureManager();
        openDevice();
    }

    private void openDevice() {
        isOpen = mSignatureManager.openDevice();
        showMessage("open device：" + isOpen);

        if(!isOpen){
            initHardware=-1;
            return;
        }
        startCaptureThread();
    }

    private void startCaptureThread() {
        Log.e(TAG,"开始线程" );
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                Log.e(TAG,"run ...." );
                isStart = mSignatureManager.StartCapture();
                if (!isStart) {
                    initHardware=-1;
                    return;
                }
                showMessage("Please sign on the signature board and submit");
//                Log.e(TAG,"getCaptureDataFromUart begin" );
                byte[] signPicture = mSignatureManager
                        .getCaptureDataFromUart();
                if (signPicture == null) {
                    signatureBmp = null;
                    LogUtils.e(TAG, "getCaptureDataFromUart is null");
                    showMessage(getResources().getString(R.string.signature_fail));
                    return;
                }
                signPicture = mSignatureManager
                        .getPictureByte(signPicture);
                if (signPicture == null) {
                    LogUtils.e(TAG, "getPictureByte fail");
                    showMessage(getResources().getString(R.string.signature_fail));
                    return;
                }
                signatureBmp = mSignatureManager
                        .getBmpFormByte(signPicture);
                Log.e(TAG, "signatureBmp:"+signatureBmp);
                mWeakReferenceHandler.sendEmptyMessage(MSG_SHOW_BMP);
                showMessage(getResources().getString(R.string.signature_successful));
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }


    @Override
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        return mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Signture;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void CollectionResult(byte runningMode, boolean isSuccess, String successNumber) {
        super.CollectionResult(runningMode, isSuccess, successNumber);
       }

    /**
     * 接收校验包4001包
     */
    @Override
    protected void requestCollectionCheck(final SessionSigntureCheckSocket mSocketSession) {
        super.requestCollectionCheck(mSocketSession);
        //解析数据；
//        LogUtils.v(TAG, "接收到签字数据；");
//        byte mode = mSocketSession.getRunningMode();
//        String userNumber = mSocketSession.getUserNumber();
//        byte[] pictureData = mSocketSession.getSingturePictureData();
//        String pictureNumber = mSocketSession.getRecPhotoNumber();
//        String ID = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
//        byte[] isSuccess = {0x01};
//        if (mode == MessageType.TYPE_MODE.Signture && ID.equals(userNumber)) {
//            //保存数据到数据库
//            if (saveSigntureToDB(userNumber, pictureNumber, pictureData)) {
//                isSuccess[0] = 0x00;
//            }
//        }
//        //发送应答
//        if (sendMessageToPC(mSocketSession, mSocketSession.getMessageToClient(isSuccess))) {
//            requestFinishActivity();
//        } else {
//            LogUtils.v(AppConfig.MODULE_APP, "4002包数据没有成功发送到PC端；");
//        }

    }

    /**
     * 保存图片到数据库中
     *
     * @param UserNumber    用户ID
     * @param pictureNumber 图片编号
     * @param pictureData   图片数据；
     * @return
     */
    private boolean saveSigntureToDB(String UserNumber, String pictureNumber, byte[] pictureData) {
        Bitmap bitmap = BitmapUtil.Bytes2Bimap(pictureData);
        if (bitmap == null) {
            LogUtils.v(AppConfig.MODULE_APP, "图片数据无法还原bmp文件；");
            return false;
        }
        String saveFileName = "Signture_" + pictureNumber + "_" + DateUtil.getCurrentTime();
        File file = BitmapUtil.saveBitmapToPngFile(bitmap, AppConfig.WORK_SIGNTURE_PATH, saveFileName);
        if (file == null) {
            LogUtils.v(AppConfig.MODULE_APP, "bmp文件无法保存；");
            return false;
        }
        return DbHelper.get(App.get()).insertPictureInfo(UserNumber, AppConfig.PREFERENCE_KEY_SIGNTURE, file.getPath(), pictureNumber);
    }


    /**
     * 初始化硬件；
     *
     * @return
     */
    public boolean initHardWare() {
        long startTime = System.currentTimeMillis();
        while (!isOpen || !isStart) {
            if(initHardware==-1){
                return false;
            }
            if (System.currentTimeMillis() - startTime > 2000) {
                return false;
            }
            SystemClock.sleep(200);
        }
        return true;
    }

    @Override
    public byte[] getPreviewData(SocketSession socketSession) {
        return new byte[0];
    }


    @Override
    public byte[] collection(byte runningMode) {
        return null;
    }
}
