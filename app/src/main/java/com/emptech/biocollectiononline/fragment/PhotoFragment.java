package com.emptech.biocollectiononline.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.emptech.biocollectiononline.AppConfig;
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
import com.emptech.biocollectiononline.views.WaveView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import butterknife.BindView;
import cn.xiongdi.jni.UserDevices;

public class PhotoFragment extends BaseSocketFragment implements IPreview, Camera.PreviewCallback, SurfaceHolder.Callback {

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;

    @BindView(R.id.wave)
    WaveView mWaveView;

    @BindView(R.id.num_tv)
    TextView mNumtv;

    @BindView(R.id.face_view)
    FaceView mFaceView;

    @BindView(R.id.tv_photo_tip)
    TextView tv_photo_tip;

    @BindView(R.id.takephoto_tip)
    TextView takephoto_tip;

    private int initHardware = -100;
    private final static int MAX_FPS = 15;    //视频通话控制在15帧是足够的
    private final static int FRAME_PERIOD = (1000 / MAX_FPS); // the frame period
    Camera mCamera;
    private final int CAMERA_PERMISSION_CODE = 0x01;

    int mPreviewWidth;

    int mPreviewHeight;

    int mRealPreviewWidth;

    int mRealPreviewHeight;

//    int mPictureWidth = 4224;
//    int mPictureHeight = 3136;

    int mPictureWidth = 3264;
    int mPictureHeight = 2448;

    CheckCameraFaceAdapter faceCheck;
    private SocketSession mCollectionSocketSession;
    private int previewWidth = 800;
    private int previewHeight = 600;
    private HandlerThread handlerThread;
    private Handler handler;
    private long lastTimeDown;
    private long lastTimeUp;
    byte[] previewData;
    private String error;
    private double lastMeterTime;
    private ByteArrayOutputStream outstream;
    private Rect rectangle;

    @Override
    protected int getLayout() {
        return R.layout.fragment_open_cv;
    }

    @Override
    protected void initView(View view) {
        outstream = new ByteArrayOutputStream();
        rectangle = new Rect(0, 0, previewWidth, previewHeight);
        setIPreviewListener(this);
        faceCheck = new CheckCameraFaceAdapter();
        faceCheck.init();
        faceCheck.setFaceLinstener(mFaceListener);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // 已有权限
            openCamera();
        }
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

        mWaveView.setDuration(6000);
        mWaveView.setInitialRadius(mWaveView.getMeasuredWidth() / 2);
        mWaveView.setStyle(Paint.Style.FILL);
        mWaveView.setColor(Color.parseColor("#ffff0000"));
        mWaveView.setMaxRadiusRate(0.98f);
        mWaveView.setInterpolator(new LinearOutSlowInInterpolator());
        mWaveView.start();

//        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(mCamera!=null){
//                    setCameraMeteringAreas(event.getX(),event.getY() );
//                    return true;
//                }
//                return false;
//            }
//        });
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


    private void takePhoto() {
        if (mCamera != null) {
            LogUtils.e(TAG, "mCamera.takePicture start");
            final long startphotoTime = System.currentTimeMillis();
            LogUtils.v(TAG, "拍照开始：" + startphotoTime);
            mCamera.stopPreview();
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //发送数据
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
    protected void startCollection(SocketSession mSocketSession) {
        mCollectionSocketSession = mSocketSession;
        LogUtils.e(TAG, "start take photo command.isTakePhoto:" + isTakePhoto);
        if (!isTakePhoto) {
            isTakePhoto = true;
            mWeakReferenceHandler.sendEmptyMessage(MSG_TAKEPHOTO_BEGIN);
            startCountDownTime();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void openCamera() {
        LedManager.open();
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
            mCamera = Camera.open(index);
            mSurfaceView.getHolder().addCallback(this);
        } catch (Exception e) {
            LogUtils.e(TAG, "mCamera open fail"+e.getMessage());
            initHardware = -1;
            e.printStackTrace();
        }
    }


    private void startPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            } catch (Exception e) {
                e.printStackTrace();
            }

            mCamera.setDisplayOrientation(270);
            mCamera.setPreviewCallbackWithBuffer(this);
//            for (int i = 0; i < 3; i++) {
             mCamera.addCallbackBuffer(new byte[((previewWidth * previewHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
//            }
            mCamera.startPreview();
            LogUtils.v(TAG, "开始预览");
            isTakePhoto = false;
        }
    }

    private void setTextPhotoTip(String message) {
        if (TextUtils.isEmpty(message)) {
            tv_photo_tip.setText(null);
        } else {
            tv_photo_tip.setText(message);
        }

    }


    private void autoCamera(int left, int top, int right, int bottom) {
        int type = faceCheck.canTakePhoto(left, top, right, bottom, mPreviewWidth, mPreviewHeight);
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
            default:
                motormove(UserDevices.actionMoveStop);
                break;
        }
        mFaceView.setStatus(stutas);
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
        if (UserDevices.currentSate != status && Math.abs(lastTimeDown - lastTimeUp) >= 200) {
            UserDevices.motor_move(status, 99);
            UserDevices.currentSate = status;
        }
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



    private void release() {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(null);
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        UserDevices.motor_move(UserDevices.actionMoveStop, 99);
        LedManager.close();
    }

    @Override
    public void closePreview() {
        LogUtils.e(TAG, "recieve close modle：finish");
        requestFinishActivity();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null == data) {
            camera.addCallbackBuffer(new byte[((previewWidth * previewHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
            return;
        } else {
            camera.addCallbackBuffer(data);
        }
//        previewData = data;
        try {
            faceCheck.process(data, previewWidth, previewHeight);
        } catch (Exception e) {
            LogUtils.e(TAG, "onPreviewFrame process:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean getYUVdata(byte[] bytes, int width, int height) {
        if (bytes.length < width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8) {
            return false;
        }
        byte[] yBytes = new byte[width * height];
        //临时存储uv数据的
        byte uBytes[] = new byte[width * height / 4];
        byte vBytes[] = new byte[width * height / 4];
        int index = 0;
        System.arraycopy(bytes, 0, yBytes, index, width * height);
        index += width * height;
        System.arraycopy(bytes, index, uBytes, 0, width * height / 4);
        index += width * height / 4;
        System.arraycopy(bytes, index, vBytes, 0, width * height / 4);
        ByteBuffer mYBuffer = ByteBuffer.allocateDirect(yBytes.length)
                .order(ByteOrder.nativeOrder());
        mYBuffer.put(yBytes);

        ByteBuffer mUBuffer = ByteBuffer.allocateDirect(uBytes.length)
                .order(ByteOrder.nativeOrder());
        mUBuffer.put(uBytes);

        ByteBuffer mVBuffer = ByteBuffer.allocateDirect(vBytes.length)
                .order(ByteOrder.nativeOrder());
        mVBuffer.put(vBytes);
        return true;
    }

    private byte[] cropImage(byte[] data, int dstwidth, int dstheight) {
        byte[] dst = new byte[dstwidth * dstheight * 3 / 2];
        int widthoffset = (previewWidth - dstwidth) / 2;
        int heightoffset = (previewHeight - dstheight) / 2;
        for (int i = 0; i < previewHeight; i++) {
            for (int j = 0; j < previewWidth; j++) {
                dst[dstwidth * i + j] = data[previewWidth * (i + heightoffset) + (j + widthoffset)];
            }
        }
        int src_y_length = previewWidth * previewHeight;
        int dst_y_length = dstwidth * dstheight;
        int index = dst_y_length;
        int src_begin = src_y_length + heightoffset * previewWidth / 4;
        int src_u_length = src_y_length / 4;
        int dst_u_length = dst_y_length / 4;
        for (int i = 0; i < dstheight / 2; i++)
            for (int j = 0; j < dstwidth / 2; j++) {
                int p = src_begin + i * (previewWidth >> 1) + (widthoffset >> 1) + j;
                dst[index] = data[p];
                dst[dst_u_length + index++] = data[p + src_u_length];
            }
        return dst;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        faceCheck.setmPreviewSize(previewWidth, previewHeight);
        if (mCamera == null) {
            openCamera();
        }
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//        parameters.setPreviewFpsRange(1, 5); // 每秒显示10~15帧
        parameters.setPictureFormat(ImageFormat.JPEG);//JPEG
        Camera.Size bestPreviewSize = getBestPreviewSize(width, height, parameters);
        LogUtils.v(AppConfig.MODULE_APP, "宽:" + bestPreviewSize.width + ";高:" + bestPreviewSize.height);
//        parameters.setPreviewSize(800, 600);
//        parameters.setPreviewSize(2112, 1568);
        parameters.setPreviewSize(previewWidth, previewHeight);
        Camera.Size bestPictureSize = getBestPictureSize(width, height, parameters);
        LogUtils.v(AppConfig.MODULE_APP, "宽:" + bestPictureSize.width + ";高:" + bestPictureSize.height);
        parameters.setRotation(90);
        int maxZoom = parameters.getMaxZoom();
        Log.v(TAG, "Zoom:" + maxZoom);
        parameters.setZoom(9);
        parameters.setPictureSize(mPictureWidth, mPictureHeight);
        mRealPreviewWidth = previewWidth;
        mRealPreviewHeight = previewHeight;
        mCamera.setParameters(parameters);
        startPreview();


    }


    private Camera.Size getBestPictureSize(int disPlayWidth, int disPlayHeight, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        if (parameters.getPictureSize().height > parameters.getPictureSize().width) {
            if (disPlayHeight < disPlayWidth) {
                int temp = disPlayWidth;
                disPlayWidth = disPlayHeight;
                disPlayHeight = temp;
            }
        } else {
            // 横屏
            if (disPlayHeight > disPlayWidth) {
                int temp = disPlayWidth;
                disPlayWidth = disPlayHeight;
                disPlayHeight = temp;
            }
        }

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            LogUtils.v(TAG, "支持拍照像素：" + size.width + "x" + size.height);
            if (size.width >= disPlayWidth && size.height >= disPlayHeight) {
                if (bestSize == null) {
                    bestSize = size;
                    continue;
                }
                if (bestSize.width * bestSize.height > size.width * size.height) {
                    bestSize = size;
                }
            }
        }
        return bestSize;
    }


    private Camera.Size getBestPreviewSize(int disPlayWidth, int disPlayHeight, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        if (parameters.getPreviewSize().height > parameters.getPreviewSize().width) {
            if (disPlayHeight < disPlayWidth) {
                int temp = disPlayWidth;
                disPlayWidth = disPlayHeight;
                disPlayHeight = temp;
            }
        } else {
            // 横屏
            if (disPlayHeight > disPlayWidth) {
                int temp = disPlayWidth;
                disPlayWidth = disPlayHeight;
                disPlayHeight = temp;
            }
        }

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            LogUtils.v(TAG, "像素：" + size.width + "x" + size.height);
            if (size.width >= disPlayWidth && size.height >= disPlayHeight) {
                if (bestSize == null) {
                    bestSize = size;
                    continue;
                }
                if (bestSize.width * bestSize.height > size.width * size.height) {
                    bestSize = size;
                }
            }
        }
        return bestSize;
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        release();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
            startPreview();
        } else {
            Toast.makeText(getContext(), "reject by user", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean isInitPreviewMode(SocketSession mSocketSession) {
        return mSocketSession.getRunningMode() == MessageType.TYPE_MODE.Photo;
    }


    /**
     * 初始化硬件；
     *
     * @return
     */
    @Override
    public boolean initHardWare() {
        long startTime = System.currentTimeMillis();
        while (mCamera == null) {
            if (initHardware == -1)
                return false;
            if ((System.currentTimeMillis() - startTime) > 3000) {
                return false;
            }
            SystemClock.sleep(20);
        }
        return true;
    }


    /**
     * 获取预览数据；
     *
     * @return
     */
    @Override
    public byte[] getPreviewData(SocketSession socketSession) {
        if (previewData == null) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        LogUtils.e(TAG, "the previewData：" + previewData.length);
        YuvImage image = new YuvImage(previewData, ImageFormat.NV21,
                mRealPreviewWidth, mRealPreviewHeight, null);
        outstream.reset();
        image.compressToJpeg(rectangle, 20, outstream);
        long stoptime = System.currentTimeMillis();
        LogUtils.e(TAG, "the use time of comparess：" + (stoptime - startTime));
        return getParmPictureData(previewWidth, previewHeight, 1, outstream.toByteArray());
    }


    @Override
    public byte[] collection(byte runningMode) {

//        if (runningMode == MessageType.TYPE_MODE.Photo && mTakePhotoCache != null) {
//            mTakePhotoCache = BitmapUtil.rotateBitmap(mTakePhotoCache, 90);
//            return getParmPictureData(mTakePhotoCache.getWidth(), mTakePhotoCache.getHeight(), 1, BitmapUtil.Bitmap2Bytes(mTakePhotoCache));
//        }
        return null;
    }


    @Override
    public void CollectionResult(byte runningMode, boolean isSuccess, String successNumber) {
        LogUtils.e(TAG, "CollectionResult is called");
        super.CollectionResult(runningMode, isSuccess, successNumber);
        if (runningMode == MessageType.TYPE_MODE.Photo) {
            //图片验证成功；
            if (isSuccess) {
//               mWeakReferenceHandler.removeCallbacks(runnable);
            }
        }
    }


    /**
     * 保存图片数据到数据库中
     *
     * @param ；
     * @return 是否保存成功
     */
//    private boolean savePhotoTODB(String successNumber) {
//        String saveNumber = successNumber;
//        String saveFileKey = AppConfig.PREFERENCE_KEY_PHOTO;
//        String saveFileValue = "photo_" + successNumber + "_" + DateUtil.getCurrentTime();
//        File filePng = BitmapUtil.saveBitmapToPngFile(mTakePhotoCache, AppConfig.WORK_PHOTO_PATH, saveFileValue);
//        String IDNumber = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
//        return DbHelper.get(App.get()).insertPictureInfo(IDNumber, saveFileKey, filePng.getPath(), saveNumber);
//    }
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


    private Timer countDownTimer = null;//自动拍照定时任务
    private TimerTask mTimerTask = null;//自动拍照定时任务
    private final static int TAKEPHOTO_COUNTDOWN_TIME = 3;//拍照倒计时
    private int countDownTime = TAKEPHOTO_COUNTDOWN_TIME;//倒计时

    private boolean isTakePhoto = false;//是否已经拍照

    /**
     * 倒计时；
     */
    class countDownTimerTask extends TimerTask {
        @Override
        public void run() {
            Message msg = Message.obtain();
            msg.what = MSG_COUNTDOWN_SHOW;
            msg.obj = countDownTime;
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
            countDownTimer.schedule(mTimerTask, 0, 1000);
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
    public void onStop() {
        super.onStop();
    }

    @SuppressLint("NewApi")
    @Override
    public void onDestroyView() {
        handlerThread.quitSafely();
        faceCheck.setFaceLinstener(null);
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
        super.onDestroyView();
    }

    private final static int MSG_COUNTDOWN_SHOW = 1001;
    private final static int MSG_TAKEPHOTO_BEGIN = 1002;
    private final static int MSG_TAKEPHOTO_OVER = 1003;

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_COUNTDOWN_SHOW:
                        mNumtv.setVisibility(View.VISIBLE);
                        mWaveView.setVisibility(View.GONE);
                        int count = (Integer) msg.obj;
                        mNumtv.setText(count + "");
                        break;
                    case MSG_TAKEPHOTO_BEGIN:
                        tv_photo_tip.setVisibility(View.GONE);
                        takephoto_tip.setVisibility(View.VISIBLE);
                        takephoto_tip.setText(R.string.takephoto);
                        break;
                    case MSG_TAKEPHOTO_OVER:
                        takephoto_tip.setVisibility(View.VISIBLE);
                        takephoto_tip.setText(R.string.process);
                        mNumtv.setVisibility(View.GONE);
                        mWaveView.setColor(Color.GREEN);
                        mWaveView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
    }
}
