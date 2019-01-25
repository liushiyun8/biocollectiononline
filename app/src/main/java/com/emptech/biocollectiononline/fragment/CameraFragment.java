package com.emptech.biocollectiononline.fragment;

import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cbly.jpegturbo.TJ;
import com.cbly.jpegturbo.TurboJpegLib;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.FaceListener;
import com.emptech.biocollectiononline.common.MusicPlayer;
import com.emptech.biocollectiononline.manager.CheckCameraFaceAdapter;
import com.emptech.biocollectiononline.manager.LedManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.views.FaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import butterknife.BindView;
import cn.xiongdi.jni.UserDevices;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends BaseSocketFragment implements IPreview, Camera.PreviewCallback, SurfaceHolder.Callback, TextureView.SurfaceTextureListener {


    @BindView(R.id.num_tv)
    TextView numTv;
    @BindView(R.id.wave)
    GifImageView mWaveView;
    @BindView(R.id.tv_photo_tip)
    TextView tv_photo_tip;
    @BindView(R.id.takephoto_tip)
    TextView takephoto_tip;
    @BindView(R.id.surface_view)
    TextureView mSurfaceView;
    @BindView(R.id.face_view)
    FaceView mFaceView;
//    @BindView(R.id.tip_iv)
//    ImageView iv;
    private Camera.Parameters parameters;
    private int initHardware = -100;
    Camera mCamera;

    int mPreviewWidth=800;
    int mPreviewHeight=600;

    int mDevicePreWidth;
    int mDevicePreHeight;
    int mPictureWidth = 3264;
    int mPictureHeight = 2448;

    CheckCameraFaceAdapter faceCheck;
    private SocketSession mCollectionSocketSession;
    private HandlerThread handlerThread;
    private Handler handler;
    private long lastTimeDown;
    private long lastTimeUp;
    byte[] previewData;
    private String error;
    private double lastMeterTime;
    private ByteArrayOutputStream outstream;
    private Rect rectangle;

    private final static int MSG_COUNTDOWN_SHOW = 1001;
    private final static int MSG_TAKEPHOTO_BEGIN = 1002;
    private final static int MSG_TAKEPHOTO_OVER = 1003;

    private Timer countDownTimer = null;//自动拍照定时任务
    private TimerTask mTimerTask = null;//自动拍照定时任务
    private final static int TAKEPHOTO_COUNTDOWN_TIME = 4;//拍照倒计时
    private int countDownTime = TAKEPHOTO_COUNTDOWN_TIME;//倒计时

    private boolean isTakePhoto = false;//是否已经拍照
    private AlertDialog dialog;
    private int trycount;
    private byte[] mMaxJpeg;
    private byte[] temp;


    public CameraFragment() {
    }


    @Override
    protected int getLayout() {
        return R.layout.fragment_open_cv;
    }

    @Override
    protected void initView(View view) {
        setIPreviewListener(this);
        mSurfaceView.setSurfaceTextureListener(this);
        outstream = new ByteArrayOutputStream();
        rectangle = new Rect(0, 0, mPreviewWidth, mPreviewHeight);
//        mWaveView.setDuration(6000);
//        mWaveView.setInitialRadius(mWaveView.getMeasuredWidth() / 2);
//        mWaveView.setStyle(Paint.Style.FILL);
//        mWaveView.setColor(Color.parseColor("#ffff0000"));
//        mWaveView.setMaxRadiusRate(0.98f);
//        mWaveView.setInterpolator(new LinearOutSlowInInterpolator());
//        mWaveView.start();
//        mWaveView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog();
//            }
//        });

        faceCheck = new CheckCameraFaceAdapter();
        faceCheck.init();
        faceCheck.setFaceLinstener(mFaceListener);

        handlerThread = new HandlerThread("takePhoto");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    LogUtils.e(TAG, "recieve command,start takePhoto");
                    takePhoto();
                    sendEmptyMessageDelayed(2, 500);
                } else if (msg.what == 2) {
                    MusicPlayer.get(activity).play(R.raw.take_photo1, false);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showDialog() {
        if(dialog==null){
            final List<String> sizes=new ArrayList<>();
            if(parameters!=null){
                for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                    LogUtils.v(TAG, "支持拍照像素：" + size.width + "x" + size.height);
                    sizes.add( size.width + "x" + size.height);
                }
            }
            String[] s = new String[sizes.size()];
            dialog = new AlertDialog.Builder(activity).setTitle("拍照分辨率").setItems(sizes.toArray(s), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] xes = sizes.get(which).split("x");
                    Log.e("TAG", Integer.parseInt(xes[0])+"x"+Integer.parseInt(xes[1]));
                    parameters.setPictureSize(Integer.parseInt(xes[0]),Integer.parseInt(xes[1]));
                    mCamera.setParameters(parameters);
                }
            }).create();
        }
        dialog.show();
    }

    private void takePhoto() {
        if (mCamera != null) {
            LogUtils.e(TAG, "mCamera.takePicture start");
            final long startphotoTime = System.currentTimeMillis();
            LogUtils.v(TAG, "拍照开始：" + startphotoTime);
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //发送数据
                    mCamera.stopPreview();
                    long stopphotoTime = System.currentTimeMillis();
                    Log.v(TAG, "拍照结束：" + stopphotoTime + ",用时:" + (stopphotoTime - startphotoTime) + "ms");

                    mWeakReferenceHandler.sendEmptyMessage(MSG_TAKEPHOTO_OVER);
                    Camera.Size size = camera.getParameters().getPictureSize();
                    LogUtils.e(TAG, "takePicture data：" + data.length + ";width:" + size.width + ";height:" + size.height);
                    byte[] pictureData = getParmPictureData(mPictureWidth, mPictureHeight, 1, data);
                    sendCollectionResult(mCollectionSocketSession, pictureData);
                }
            });
        }
    }

    @Override
    public void closePreview() {
        super.closePreview();
        LogUtils.e(TAG,"closePreview" );
        requestFinishActivity();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(data!=null){
            previewData=data;
            if(!isTakePhoto){
                long start=System.currentTimeMillis();
                faceCheck.process(data, mPreviewWidth, mPreviewHeight);
                long end = System.currentTimeMillis();
                Log.e(TAG,"process use time:"+(end-start)+"ms" );
            }
            camera.addCallbackBuffer(data);
        }else {
            camera.addCallbackBuffer(new byte[((mPreviewWidth * mPreviewHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            mDevicePreWidth=width;
//            mDevicePreHeight=height;
//            faceCheck.setmPreviewSize(mDevicePreWidth,mDevicePreHeight );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//            release();
    }

    private FaceListener mFaceListener = new FaceListener() {
        @Override
        public void noFace() {
            mFaceView.setFaceArea(0, 0, 0, 0);
            error = null;
            setTextPhotoTip(error);
            mFaceView.setStatus(-1);
            motormove(UserDevices.actionMoveStop);
        }

        @Override
        public void faceIn(int leftEyeX, int leftEyeY, int rightEyeX, int rightEyeY, int mouseX, int mouseY) {
            mFaceView.setFace(leftEyeX, leftEyeY, rightEyeX, rightEyeY, mouseX, mouseY);
        }

        @Override
        public void faceArea(int left, int top, int right, int bottom) {
            mFaceView.setFaceArea(left, top, right, bottom);
            autoCamera(left, top, right, bottom);
        }
    };

    private void setTextPhotoTip(String message) {
        if (TextUtils.isEmpty(message)) {
            tv_photo_tip.setText(null);
        } else {
            tv_photo_tip.setText(message);
        }

    }

    private void autoCamera(int left, int top, int right, int bottom) {
        int type = faceCheck.canTakePhoto(left, top, right, bottom, mDevicePreWidth, mDevicePreHeight);
        setTextPhotoTip(getTextError(type));
        int stutas = 1;
        switch (type) {
            case CheckCameraFaceAdapter.FACE_OK:
                motormove(UserDevices.actionMoveStop);
                if (System.currentTimeMillis() - lastMeterTime > 1000) {
                    setCameraMeteringAreas(top, left, bottom, right);
                    lastMeterTime = System.currentTimeMillis();
                }
                stutas = 0;
                break;
            case CheckCameraFaceAdapter.FACE_DOWN:
                motormove(UserDevices.actionMoveDown);
                break;
            case CheckCameraFaceAdapter.FACE_UP:
                motormove(UserDevices.actionMoveUp);
                break;
            case CheckCameraFaceAdapter.FACE_LEFT:
                mFaceView.setDirtion(FaceView.RIGHT);
                motormove(UserDevices.actionMoveStop);
                break;
            case CheckCameraFaceAdapter.FACE_RIGHT:
                mFaceView.setDirtion(FaceView.LEFT);
                motormove(UserDevices.actionMoveStop);
                break;
            case CheckCameraFaceAdapter.FACE_NOCHANGE:
                break;
            default:
                motormove(UserDevices.actionMoveStop);
                break;
        }
        mFaceView.setStatus(stutas);
    }

    public void setCameraMeteringAreas(float left, float top, float right, float bottom) {
        Camera.Parameters parameters = mCamera.getParameters();
        Rect meteringRect = calculateTapArea(left, top, right, bottom, parameters.getPreviewSize());
        int x = meteringRect.centerX();
        int y = meteringRect.centerY();
        meteringRect = new Rect(meteringRect.left / 2, meteringRect.top / 2, meteringRect.right / 2, meteringRect.bottom / 2);
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));
            parameters.setMeteringAreas(meteringAreas);
            mCamera.setParameters(parameters);
            Log.i(TAG, "metering areas set ok!" + meteringRect + "," + parameters.getMeteringAreas().get(0).rect);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
    }

    private Rect calculateTapArea(float left, float top, float right, float bottom, Camera.Size previewSize) {
//        //计算点击坐标点在新的坐标系中的位置
        int rleft = clamp(Float.valueOf((left / previewSize.width) * 2000 - 1000).intValue());
        int rtop = clamp(Float.valueOf((top / previewSize.height) * 2000 - 1000).intValue());
        int rright = clamp(Float.valueOf((right / previewSize.width) * 2000 - 1000).intValue());
        int rbottom = clamp(Float.valueOf((bottom / previewSize.height) * 2000 - 1000).intValue());
        Log.d("CameraFocus", "measure width:" + previewSize.width + "  measure height:" + previewSize.height);
        return new Rect(rleft, rtop, rright, rbottom);
    }

    private static int clamp(int touchCoordinateInCameraReper) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000;
            } else {
                result = -1000;
            }
        } else {
            result = touchCoordinateInCameraReper;
        }
        return result;
    }

    private void motormove(byte status) {
        switch (status) {
            case UserDevices.actionMoveDown:
                lastTimeDown = System.currentTimeMillis();
                break;
            case UserDevices.actionMoveUp:
                lastTimeUp = System.currentTimeMillis();
                break;
            default:
                lastTimeUp = lastTimeDown = System.currentTimeMillis();
                if (UserDevices.currentSate != status) {
                    UserDevices.motor_move(status, 99);
                    UserDevices.currentSate = status;
                }
                return;
        }
        if (UserDevices.currentSate != status && Math.abs(lastTimeDown - lastTimeUp) >= 200&&!isTakePhoto) {
            UserDevices.motor_move(status, 99);
            UserDevices.currentSate = status;
        }
    }

    private String getTextError(int typeError) {
        int resId = 0;
        switch (typeError) {
            case CheckCameraFaceAdapter.FACE_BIG:
                error = getString(R.string.move_away_face);
                resId = R.raw.away_screen;
                break;
            case CheckCameraFaceAdapter.FACE_SMALL:
                error = getString(R.string.move_close_face);
                resId = R.raw.close_face;
                break;
            case CheckCameraFaceAdapter.FACE_RIGHT:
                error = getString(R.string.move_left_face);
                resId = R.raw.moveleft_face;
                break;
            case CheckCameraFaceAdapter.FACE_LEFT:
                error = getString(R.string.move_right_face);
                resId = R.raw.moveright_face;
                break;
            case CheckCameraFaceAdapter.FACE_DOWN:
                error = getString(R.string.move_up_face);
                break;
            case CheckCameraFaceAdapter.FACE_UP:
                error = getString(R.string.move_down_face);
                break;
            case CheckCameraFaceAdapter.FACE_NOCHANGE:
                break;
            default:
                error = getString(R.string.photo_tip);
                break;
        }
//        if(resId!=0){
//            MusicPlayer.get(activity).playNoRepeat(resId, false);
////            AudioPlay.getInstance(activity).playRaw(resId);
//        }
        return error;
    }

    @Override
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        return mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Photo;
    }

    @Override
    public boolean initHardWare() {
        long startTime = System.currentTimeMillis();
        while (mCamera == null) {
            if (initHardware == -1)
                return false;
            if ((System.currentTimeMillis() - startTime) > 5000) {
                return false;
            }
            SystemClock.sleep(20);
        }
        return true;
    }

    @Override
    public byte[] getPreviewData(SocketSession socketSession) {
        if (previewData == null) {
            return null;
        }
//        LogUtils.e(TAG, "the previewData：" + previewData.length);
//        long startTime = System.currentTimeMillis();
        //第三方压缩算法
        if(mMaxJpeg==null)
        mMaxJpeg=new byte[previewData.length];
        if(temp==null)
        temp = new byte[previewData.length];
        byte[] clonebytes = previewData.clone();
        int compressSize = TurboJpegLib.yuv2jpeg(clonebytes, temp, mPreviewWidth, mPreviewHeight,
                TJ.SAMP_420, mMaxJpeg, 20, TJ.FLAG_BOTTOMUP);

        // 下方返回的结果就是最终把nv21帧数据压缩后的jpeg的二进制字节
        byte[] jpegDataOut = Arrays.copyOfRange(mMaxJpeg, 0, compressSize);

        //系统的压缩算法
//        YuvImage image = new YuvImage(previewData, ImageFormat.NV21,
//                mPreviewWidth, mPreviewHeight, null);
//        outstream.reset();
//        image.compressToJpeg(rectangle, 20, outstream);
//        long stoptime = System.currentTimeMillis();
//        Log.e(TAG, "the use time of comparess：" + (stoptime - startTime));
        return getParmPictureData(mPreviewWidth, mPreviewHeight, 1, jpegDataOut);
    }

    @Override
    public byte[] collection(byte runningMode) {
        return new byte[0];
    }

    @Override
    public void onDestroyView() {
//        mWaveView.stop();
        super.onDestroyView();
        handlerThread.quit();
        faceCheck.setFaceLinstener(null);
        faceCheck.release();
        if (mCamera != null) {
            release();
        }
        if (outstream != null) {
            try {
                outstream.reset();
                outstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        resetCountDown();
        removeIpreviewListener();
    }

    private void getCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        LogUtils.e(TAG, "number Of Cameras:" + numberOfCameras);
        int index = 0;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                index = i;
                break;
            }
        }
        try {
            LedManager.open();
            mCamera = Camera.open(index);
        } catch (Exception e) {
            LogUtils.e(TAG, "mCamera open fail"+e.getMessage());
            trycount++;
            if(trycount<3){
                SystemClock.sleep(500);
                getCamera();
            }else {
                initHardware = -1;
                LedManager.close();
            }
            e.printStackTrace();
        }
    }

    private void startcamera(Camera mCamera,SurfaceTexture surfaceTexture){
        if(mCamera != null){
            try {
                mCamera.setPreviewCallbackWithBuffer(this);
                for (int i = 0; i < 3; i++) {
                    mCamera.addCallbackBuffer(new byte[((mPreviewWidth * mPreviewHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
                }
                mCamera.setDisplayOrientation(270);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
                for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                    LogUtils.v(TAG, "支持预览像素：" + size.width + "x" + size.height);
                }
                for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                    LogUtils.v(TAG, "支持拍照像素：" + size.width + "x" + size.height);
                }
                parameters.setPictureFormat(ImageFormat.JPEG);//JPEG
                parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
                parameters.setPictureSize(mPictureWidth, mPictureHeight);
                parameters.setRotation(90);
                parameters.setZoom(9);
                mCamera.setParameters(parameters);
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void release() {
        LedManager.close();
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        UserDevices.motor_move(UserDevices.actionMoveStop, 99);
        LedManager.close();
    }

    @Override
    protected void startCollection(SocketSession mSocketSession) {
//        super.startCollection(mSocketSession);
        mCollectionSocketSession = mSocketSession;
        LogUtils.e(TAG, "start take photo command.isTakePhoto:" + isTakePhoto);
        if (!isTakePhoto) {
            isTakePhoto = true;
            mWeakReferenceHandler.sendEmptyMessage(MSG_TAKEPHOTO_BEGIN);
            UserDevices.motor_move(UserDevices.actionMoveStop, 99);
            startCountDownTime();
        }
    }

    /**
     * 启动计时；
     */
    private void startCountDownTime() {
        if (countDownTime <= 0) {
            return;
        }
        if (mTimerTask == null) {
            mTimerTask = new countDownTimerTask();
        }
        if (countDownTimer == null) {
            countDownTimer = new Timer();
            countDownTimer.schedule(mTimerTask, 0, 800);
        }
    }

    /**
     * 复位CountDown倒计时；
     */
    private void resetCountDown() {
        if (mTimerTask != null) {
            mTimerTask.cancel();  //将原任务从队列中移除
            mTimerTask = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            countDownTime = TAKEPHOTO_COUNTDOWN_TIME;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e("TAG","width:"+width+",height:"+height );
        mDevicePreWidth=width;
        mDevicePreHeight=height;
//        faceCheck.setmPreviewSize(mDevicePreWidth,mDevicePreHeight);
        getCamera();
        startcamera(mCamera,surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 倒计时；
     */
    class countDownTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg = Message.obtain();
            msg.what = MSG_COUNTDOWN_SHOW;
            msg.obj = countDownTime;
            if(countDownTime!=4)
            mWeakReferenceHandler.sendMessage(msg);
            if (countDownTime <= 0) {
                UserDevices.motor_move(UserDevices.actionMoveStop, 99);
                handler.sendEmptyMessage(1);
                cancel();
                return;
            }
            countDownTime--;
        }
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_COUNTDOWN_SHOW:
                        numTv.setVisibility(View.VISIBLE);
                        mWaveView.setVisibility(View.GONE);
                        int count = (Integer) msg.obj;
                        numTv.setText(count + "");
                        break;
                    case MSG_TAKEPHOTO_BEGIN:
                        tv_photo_tip.setVisibility(View.GONE);
                        takephoto_tip.setVisibility(View.VISIBLE);
                        takephoto_tip.setText(R.string.takephoto);
                        break;
                    case MSG_TAKEPHOTO_OVER:
                        takephoto_tip.setVisibility(View.VISIBLE);
                        takephoto_tip.setText(R.string.process);
                        numTv.setVisibility(View.GONE);
//                        mWaveView.setColor(Color.GREEN);
                        mWaveView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
    }


}
