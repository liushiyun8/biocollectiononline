package com.emptech.biocollectiononline.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.camera.CameraGLSurfaceView;
import com.emptech.biocollectiononline.camera.PreviewAndPictureCallback;
import com.emptech.biocollectiononline.camera.utils.Rotation;
import com.emptech.biocollectiononline.camera.utils.TextureRotationUtil;
import com.emptech.biocollectiononline.camera.utils.YUVFilter;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.common.FaceListener;
import com.emptech.biocollectiononline.common.MusicPlayer;
import com.emptech.biocollectiononline.dao.DbHelper;
import com.emptech.biocollectiononline.manager.CheckCameraFaceAdapter;
import com.emptech.biocollectiononline.manager.LedManager;
import com.emptech.biocollectiononline.manager.PreferencesManager;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;
import com.emptech.biocollectiononline.socket.MessageType;
import com.emptech.biocollectiononline.socket.message.IPreview;
import com.emptech.biocollectiononline.socket.message.SessionStartModeSocket;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.BitmapUtil;
import com.emptech.biocollectiononline.utils.DateUtil;
import com.emptech.biocollectiononline.utils.FileUtil;
import com.emptech.biocollectiononline.utils.LogUtils;
import com.emptech.biocollectiononline.views.FaceView;
import com.emptech.biocollectiononline.views.WaveView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import butterknife.BindView;
import butterknife.OnClick;
import cn.xiongdi.jni.UserDevices;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class Camera2Fragment extends BaseSocketFragment implements IPreview, Camera.PreviewCallback, SurfaceHolder.Callback, CameraGLSurfaceView.CameraTextureListener, PreviewAndPictureCallback {

    @BindView(R.id.surface_view)
    CameraGLSurfaceView mSurfaceView;
    @BindView(R.id.close_tv)
    TextView closeTv;
    @BindView(R.id.wave)
    WaveView mWaveView;
    private int initHardware = -100;

    private final static int MAX_FPS = 15;    //视频通话控制在15帧是足够的
    private final static int FRAME_PERIOD = (1000 / MAX_FPS); // the frame period

    Camera mCamera;

    private final int CAMERA_PERMISSION_CODE = 0x01;

    private final String TAG = getClass().getSimpleName();


    Bitmap mTakePhotoCache;
    @BindView(R.id.num_tv)
    TextView mNumtv;

    @BindView(R.id.face_view)
    FaceView mFaceView;

    @BindView(R.id.take_photo)
    View mTakePhotoBtn;

    @BindView(R.id.tv_photo_tip)
    TextView tv_photo_tip;

    @BindView(R.id.btn_photo_shoot)
    Button btn_photo_shoot;

    @BindView(R.id.btn_photo_countdown)
    Button btn_photo_countdown;

    @BindView(R.id.photo_bottom_view)
    View photo_bottom_view;

    int mPreviewWidth;

    int mPreviewHeight;

    int mRealPreviewWidth;

    int mRealPreviewHeight;

    int mPictureWidth = 4224;
    int mPictureHeight = 3136;

    CheckCameraFaceAdapter faceCheck;
//    FaceCheck faceCheck;

    private ToneGenerator tone;
    private SocketSession mCollectionSocketSession;
    private int previewWidth = 2112;
    private int previewHeight = 1568;
    private HandlerThread handlerThread;
    private Handler handler;
    private long lastTimeDown;
    private long lastTimeUp;
    private long lastTime;
    byte[] previewData;
    private String error;
    private YUVFilter filter;
    private FloatBuffer mGLTextureBuffer;
    //    private Runnable runnable=new Runnable() {
//        @Override
//        public void run() {
//            Toast.makeText(activity,R.string.timeout ,Toast.LENGTH_SHORT ).show();
//        }
//    };

    @Override
    protected int getLayout() {
        return R.layout.camera2_fragment;
    }

    @Override
    protected void initView(View view) {
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, true, false)).position(0);
//        filter = new YUVFilter();
        faceCheck = new CheckCameraFaceAdapter();
        setIPreviewListener(this);
        faceCheck.init();
        faceCheck.setFaceLinstener(mFaceListener);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // 已有权限
//            openCamera();
        }
        handlerThread = new HandlerThread("takePhoto");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    LogUtils.e(TAG, "recieve command,start takePhoto");
                    mSurfaceView.takePhoto();
//                    takePhoto();
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
        mSurfaceView.enableView();
        mSurfaceView.setCameraTextureListener(this);
        mSurfaceView.setCameraPrevieviewAndPictureCallback(this);
    }

    @OnClick({R.id.take_photo, R.id.btn_photo_countdown, R.id.btn_photo_shoot})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.take_photo:
                MusicPlayer.get(activity).play(R.raw.take_photo1, false);
                handler.sendEmptyMessage(1);
                break;
            case R.id.btn_photo_countdown:
                selectedCountDownMode(true);
                break;
            case R.id.btn_photo_shoot:
                selectedCountDownMode(false);
                break;
//            case R.id.show_lay:
//                final Camera.Parameters parameters = mCamera.getParameters();
//                AlertDialog dialog = new AlertDialog.Builder(activity).setSingleChoiceItems(new String[]{"-2", "-1", "0", "1", "2"}, parameters.getExposureCompensation()+2, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        parameters.setExposureCompensation(which-2);
//                    }
//                }).setTitle("曝光级别选择").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        mCamera.setParameters(parameters);
//                    }
//                }).create();
//                Window window = dialog.getWindow();
//                WindowManager.LayoutParams attributes = window.getAttributes();
//                attributes.alpha=0.5f;
//                window.setAttributes(attributes);
//                dialog.show();
//                break;
        }
    }

    public void setCameraMeteringAreas(float x, float y) {
        Log.e(TAG, "x:" + x + ",y:" + y);
        Camera.Parameters parameters = mCamera.getParameters();
        Rect meteringRect = calculateTapArea(x, y, 1.5f, parameters.getPreviewSize());
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            parameters.setMeteringAreas(meteringAreas);
            mCamera.setParameters(parameters);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        int FOCUS_AREA_SIZE = 300;
//        //计算点击坐标点在新的坐标系中的位置
        Log.e(TAG, "focus position : " + x + " : " + y);
        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();
        int left = clamp(Float.valueOf((y / previewSize.width) * 2000 - 1000).intValue(), areaSize);
        int top = clamp(Float.valueOf(((previewSize.height - x) / previewSize.height) * 2000 - 1000).intValue(), areaSize);
        Log.d("CameraFocus", "measure width:" + previewSize.width + "  measure height:" + previewSize.height);
        Log.d(TAG, "previewArea:" + left + "  " + top + " " + (left + areaSize) + " " + (top + areaSize));
        return new Rect(left, top, left + areaSize, top + areaSize);
    }

    private static int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize;
            } else {
                result = -1000 + focusAreaSize;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }


    private void takePhoto() {
//        if (mCamera != null) {
////            AudioPlay.getInstance().PlayAssetFile("take_photo1.mp3",activity.getAssets() );
//            LogUtils.e(TAG,"mCamera.takePicture start" );
//            mCamera.takePicture(null, null, new Camera.PictureCallback() {
//                @Override
//                public void onPictureTaken(byte[] data, Camera camera) {
//                    //发送数据
//                    //旋转图片
////                    mCamera.startPreview();
//                    camera.stopPreview();
//                    Camera.Size size = camera.getParameters().getPictureSize();
//                    LogUtils.e(TAG, "takePicture data：" + data.length + ";width:" + size.width + ";height:" + size.height);
////                    final long time = System.currentTimeMillis();
////                    Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
////                    options.config=Bitmap.Config.RGB_565;
////                    Tiny.getInstance().source(data).asBitmap().withOptions(options).compress(new BitmapCallback() {
////                        @Override
////                        public void callback(boolean isSuccess, Bitmap bitmap, Throwable t) {
////                            mTakePhotoCache=bitmap;
////                            LogUtils.e(TAG, "转换图片用时：" + (System.currentTimeMillis() - time));
////                            if (mTakePhotoCache == null) {
////                                LogUtils.e(TAG, "当前拍照照片是空的!");
////                            }
////                            //重新开始预览
////                            mTakePhotoCache = BitmapUtil.rotateBitmap(mTakePhotoCache, 90);
////                            byte[] pictureData = getParmPictureData(mTakePhotoCache.getWidth(), mTakePhotoCache.getHeight(), 1, BitmapUtil.Bitmap2Bytes(mTakePhotoCache));
////                            sendCollectionResult(mCollectionSocketSession,pictureData);
////                            PCneedPreview=true;
////                        }
////                    });
//
////                    mTakePhotoCache =BitmapUtil.compressBitmap(data,1200 ,1024 ,50 );
////                    mTakePhotoCache = BitmapUtil.Bytes2Bimap(data);//
////                    mTakePhotoCache = BitmapUtil.compressImage(mTakePhotoCache,1024,10);
////                    LogUtils.e(TAG, "转换图片用时：" + (System.currentTimeMillis() - time));
////                    if (mTakePhotoCache == null) {
////                        LogUtils.e(TAG, "当前拍照照片是空的!");
////                    }
////                    File file = new File(AppConfig.WORK_PHOTO_PATH + "/1.jpg");
////                    try {
////                        FileOutputStream fileOutputStream = new FileOutputStream(file);
////                        fileOutputStream.write(data);
////                        fileOutputStream.flush();
////                        fileOutputStream.close();
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
//                    //图片测试
////                    byte[] bytes = FileUtil.getFileBytes(new File(AppConfig.WORK_PATH + "/liui.jpg"));
////                    LogUtils.e(TAG,"bytes:"+bytes.length );
////                    try {
////                        mTakePhotoCache.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(AppConfig.WORK_PHOTO_PATH+"/2.bmp")));
////                    } catch (FileNotFoundException e) {
////                        e.printStackTrace();
////                    }
////                    //重新开始预览
////                    mTakePhotoCache = BitmapUtil.rotateBitmap(mTakePhotoCache, 90);
////                    byte[] bytes = BitmapUtil.Bitmap2JpegBytes(mTakePhotoCache);
////                    LogUtils.e(TAG,"bytes.length:"+bytes.length );
////                    byte[] pictureData = getParmPictureData(mTakePhotoCache.getWidth(), mTakePhotoCache.getHeight(), 1, bytes);
//                    byte[] pictureData = getParmPictureData(mPictureWidth, mPictureHeight, 1, data);
//                    sendCollectionResult(mCollectionSocketSession,pictureData);
//                }
//            });
//        }
    }

    @Override
    protected void startCollection(SocketSession mSocketSession) {
//            startCountDownTime();
        mCollectionSocketSession = mSocketSession;
        LogUtils.e(TAG, "start take photo command.isTakePhoto:" + isTakePhoto);
        if (!isTakePhoto) {
//                MusicPlayer.get(activity).play(R.raw.take_photo1,false );
//                AudioPlay.getInstance().PlayAssetFile("take_photo1.mp3",activity.getAssets() );
            isTakePhoto = true;
            UserDevices.motor_move(UserDevices.actionMoveStop, 99);
            handler.sendEmptyMessage(1);
        }
    }


    //快门按下的时候onShutter()被回调
    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            if (tone == null) {
                //发出提示用户的声音
                tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
                        ToneGenerator.MAX_VOLUME);
            }
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        if (mSocketSession instanceof SessionStartModeSocket) {
            SessionStartModeSocket modeSocket = (SessionStartModeSocket) mSocketSession;
//            previewWidth = modeSocket.getPreviewWidth()==0?800:modeSocket.getPreviewWidth();
//            previewHeight = modeSocket.getPreviewHeight()==0?600:modeSocket.getPreviewWidth();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceView.onResume();
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
            LogUtils.e(TAG, "mCamera open fail");
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
            mCamera.addCallbackBuffer(new byte[((previewWidth * previewHeight) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
            countDownTime = TAKEPHOTO_COUNTDOWN_TIME;
            isTakePhoto = false;
//            if (mCamera.getParameters().getMaxNumDetectedFaces() > 0) {
//                mCamera.setFaceDetectionListener(faceCheck);
//                mCamera.startFaceDetection();
//            }
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
//        tv_photo_tip.setText(getTextError(type));
        setTextPhotoTip(getTextError(type));
        switch (type) {
            case CheckCameraFaceAdapter.FACE_OK:
                if (btn_photo_shoot.isSelected() && mTakePhotoBtn.getVisibility() != View.VISIBLE) {
                    //选中shoot拍照方式，则显示出拍照按钮
                    setTakePhotoBtnVisibility(true);
                }
                motormove(UserDevices.actionMoveStop);
                //自动检测人脸并拍照
//                if (btn_photo_countdown.isSelected()) {
//                    startCountDownTime();
//                }
//                setCameraMeteringAreas((right + left) / 2, (bottom + top) / 2);
                break;
            case CheckCameraFaceAdapter.FACE_DOWN:
                motormove(UserDevices.actionMoveDown);
                restCountDownTime();
                break;
            case CheckCameraFaceAdapter.FACE_UP:
                motormove(UserDevices.actionMoveUp);
                restCountDownTime();
                break;
            case CheckCameraFaceAdapter.FACE_LEFT:
                mFaceView.setDirtion(FaceView.RIGHT);
                motormove(UserDevices.actionMoveStop);
                restCountDownTime();
                break;
            case CheckCameraFaceAdapter.FACE_RIGHT:
                mFaceView.setDirtion(FaceView.LEFT);
                motormove(UserDevices.actionMoveStop);
                restCountDownTime();
                break;
            default:
                motormove(UserDevices.actionMoveStop);
                restCountDownTime();
                break;
        }
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

    private void setTakePhotoBtnVisibility(boolean isVisibility) {
        if (photo_bottom_view.getVisibility() == View.VISIBLE && btn_photo_shoot.isSelected()) {
            mTakePhotoBtn.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        }
    }

    private void restCountDownTime() {

        if (btn_photo_shoot.isSelected()) {
            setTakePhotoBtnVisibility(false);
        } else {
            if (countDownTime <= 0) {
                //倒计时小于0 ，说明已经进行了拍摄
                mNumtv.setVisibility(View.GONE);
                return;
            }
            resetCountDown();
            mNumtv.setVisibility(View.INVISIBLE);
        }

    }


    private FaceListener mFaceListener = new FaceListener() {
        @Override
        public void noFace() {
            mWeakReferenceHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFaceView.setFaceArea(0, 0, 0, 0);
                    setTakePhotoBtnVisibility(false);
                    error = null;
                    setTextPhotoTip(error);
                }
            });
            resetCountDown();
            motormove(UserDevices.actionMoveStop);
        }

        @Override
        public void faceIn(final int leftEyeX, final int leftEyeY, final int rightEyeX, final int rightEyeY, final int mouseX, final int mouseY) {
            mWeakReferenceHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFaceView.setFace(leftEyeX, leftEyeY, rightEyeX, rightEyeY, mouseX, mouseY);
                }
            });
        }

        @Override
        public void faceArea(final int left, final int top, final int right, final int bottom) {
            mWeakReferenceHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFaceView.setFaceArea(left, top, right, bottom);
                    autoCamera(left, top, right, bottom);
                }
            });

        }
    };

    private void stopPreview() {
        if (mCamera != null) {
//            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
        UserDevices.motor_move(UserDevices.actionMoveStop, 99);
        LedManager.close();
    }


    private void release() {
        resetCountDown();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
//        if (mSendBitmapThread != null) {
//            mSendBitmapThread.interrupt();
//        }
        UserDevices.motor_move(UserDevices.actionMoveStop, 99);
        LedManager.close();
    }

    /**
     * 预览照片通过压缩传输给PC端；
     *
     * @param data
     * @param camera
     */
    long sendLastTime = System.currentTimeMillis();

    private void sendBitmap(byte[] photoData, final int width, final int height) {
        if (photoData == null || photoData.length == 0) {
            return;
        }
//        if (System.currentTimeMillis() - sendLastTime > 100) {
        YuvImage image = new YuvImage(photoData, ImageFormat.NV21,
                width, height, null);
        final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        LogUtils.v(AppConfig.MODULE_APP, "current preview：" + width + "," + height);
//            LogUtils.e(TAG,"previewWidth:"+previewWidth+",previeewHeight:"+previewHeight );
        long startTime = System.currentTimeMillis();
        image.compressToJpeg(
                new Rect(0, 0, width, height), 15, outstream);
//            if(isTakePhoto){
//                mTakePhotoCache = BitmapUtil.Bytes2Bimap(outstream.toByteArray());
//                if(AppConfig.PHOTO_ROTATE!=0){
//                    mTakePhotoCache =BitmapUtil.rotateBitmap(mTakePhotoCache, AppConfig.PHOTO_ROTATE);
//                }
//                byte[] pictureData = getParmPictureData(mTakePhotoCache.getWidth(), mTakePhotoCache.getHeight(), 1, BitmapUtil.Bitmap2Bytes(mTakePhotoCache));
//                sendCollectionResult(mCollectionSocketSession,pictureData);
//                isTakePhoto=false;
//            }
//
        //旋转图片
//            long startTime = System.currentTimeMillis();
//            Bitmap bitmap = BitmapUtil.Bytes2Bimap(outstream.toByteArray());
//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, true);
//            byte[] data = BitmapUtil.Bitmap2JpegBytes(scaledBitmap);
//            Bitmap rotateBitmap = BitmapUtil.rotateBitmap(BitmapUtil.Bytes2Bimap(outstream.toByteArray()), 90);
//            byte[] data =  BitmapUtil.compressImageToByte(rotateBitmap,50,20);
//            LogUtils.v(AppConfig.MODULE_APP, "旋转用时：" + (System.currentTimeMillis() - startTime) + "；原来图片大小：" + outstream.toByteArray().length + ";旋转完成后大小：" + data.length);
        //发送数据到PC端
//            sendPreviewToPC(MessageType.TYPE_MODE.Photo, getParmPictureData(rotateBitmap.getWidth(), rotateBitmap.getHeight(), 1, data));
        long stopTime = System.currentTimeMillis();
        LogUtils.e(TAG, "UseTime:" + (stopTime - startTime));

        //测试暂时关闭
        sendPreviewToPC(MessageType.TYPE_MODE.Photo, getParmPictureData(previewWidth, previewHeight, 1, outstream.toByteArray()));
//            sendLastTime = System.currentTimeMillis();
//        } else {
//            LogUtils.v(AppConfig.MODULE_APP, "图片发送太快了...");
//        }
    }

    @Override
    public void closePreview() {
        LogUtils.e(TAG, "recieve close modle：finish");
        requestFinishActivity();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
//        byte[] da=cropImage(data,previewWidth/2,previewHeight/2);
        try {
            faceCheck.process(data, previewWidth, previewHeight);
        } catch (Exception e) {
            LogUtils.e(TAG, "onPreviewFrame process:" + e.getMessage());
            e.printStackTrace();
        }
//        Canvas canvas = mSurfaceView.getHolder().lockCanvas();
//        canvas.set
        previewData = data;
//        long timeDiff = System.currentTimeMillis() - lastTime;
//        if (timeDiff < FRAME_PERIOD){
//            LogUtils.e(TAG, "被忽略的previewdate");
//            return;
//         }
//        lastTime=System.currentTimeMillis();
//        if (PCneedPreview) {
//            sendBitmap(data, mRealPreviewWidth, mRealPreviewHeight);
//        }
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
        faceCheck.setmPreviewSize(width, height);
        if (mCamera == null) {
//            openCamera();
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
//        parameters.setPictureSize(1600, 1200);
//        parameters.setPictureSize(2592, 1944);
        parameters.setRotation(90);
        parameters.setPictureSize(mPictureWidth, mPictureHeight);
        mCamera.setParameters(parameters);

        startPreview();
        mRealPreviewWidth = parameters.getPreviewSize().width;
        mRealPreviewHeight = parameters.getPreviewSize().height;

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

    /**
     * 是否选中自动拍照
     *
     * @param isCountDown
     */
    private void selectedCountDownMode(boolean isCountDown) {
        if (isCountDown) {
            if (!btn_photo_countdown.isSelected()) {
                //自动模式被选中；
                btn_photo_countdown.setSelected(true);
                btn_photo_shoot.setSelected(false);
                setTakePhotoBtnVisibility(false);
            }
        } else {
            resetCountDown();
            btn_photo_countdown.setSelected(false);
            btn_photo_shoot.setSelected(true);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        stopPreview();
        release();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            openCamera();
//            startPreview();
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
        if (mCamera != null && isTakePhoto) {
            startPreview();
            isTakePhoto = false;
        }
        if (previewData == null) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        LogUtils.e(TAG, "the previewData：" + previewData);
        YuvImage image = new YuvImage(previewData, ImageFormat.NV21,
                mRealPreviewWidth, mRealPreviewHeight, null);
        final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        LogUtils.v(AppConfig.MODULE_APP, "当前照片预览：" + mRealPreviewWidth + "," + mRealPreviewHeight);
        image.compressToJpeg(
                new Rect(0, 0, mRealPreviewWidth, mRealPreviewHeight), 20, outstream);
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
     * @param successNumber 成功编号；
     * @return 是否保存成功
     */
    private boolean savePhotoTODB(String successNumber) {
        String saveNumber = successNumber;
        String saveFileKey = AppConfig.PREFERENCE_KEY_PHOTO;
        String saveFileValue = "photo_" + successNumber + "_" + DateUtil.getCurrentTime();
        File filePng = BitmapUtil.saveBitmapToPngFile(mTakePhotoCache, AppConfig.WORK_PHOTO_PATH, saveFileValue);
        String IDNumber = PreferencesManager.getIns(App.get()).getStringPref(AppConfig.PREFERENCE_KEY_IDNUMBER);
        return DbHelper.get(App.get()).insertPictureInfo(IDNumber, saveFileKey, filePng.getPath(), saveNumber);
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
//                error = getString(R.string.move_up_face);
                break;
            case CheckCameraFaceAdapter.FACE_UP:
//                error = getString(R.string.move_down_face);
                break;
            case CheckCameraFaceAdapter.FACE_INCLINED:
            case CheckCameraFaceAdapter.FACE_INVERSION:
//                error = getString(R.string.move_aim_face);
                break;
            default:
                error = getString(R.string.photo_tip);
                break;
        }
        if (resId != 0) {
            MusicPlayer.get(activity).playNoRepeat(resId, false);
//            AudioPlay.getInstance(activity).playRaw(resId);
        }
        return error;
    }


    private Timer countDownTimer = null;//自动拍照定时任务
    private TimerTask mTimerTask = null;//自动拍照定时任务
    private final static int TAKEPHOTO_COUNTDOWN_TIME = 3;//拍照倒计时
    private int countDownTime = TAKEPHOTO_COUNTDOWN_TIME;//倒计时
    private boolean isTakePhoto = false;//是否已经拍照

    @Override
    public void onCameraViewStarted(int width, int height) {
//        filter.init();
    }

    @Override
    public void onCameraViewStopped() {

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCameraTexture(int texIn, int texOut, int width, int height) {
//        filter.onDrawFrame(texIn, , )
        return true;
    }

    @Override
    public void onPreviewData(byte[] data, int width, int height) {
        faceCheck.process(data, width, height);
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(AppConfig.WORK_TEMP_PATH + "/hi.jpg"));
            yuvImage.compressToJpeg(
                    new Rect(0, 0, width, height), 20, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTakePhtoData(byte[] data, int width, int height) {
        LogUtils.e(TAG, "takePicture data：" + data.length + ";width:" + width + ";height:" + height);
        byte[] pictureData = getParmPictureData(width, height, 1, data);
        sendCollectionResult(mCollectionSocketSession, pictureData);
        FileUtil.byte2File(data,AppConfig.WORK_TEMP_PATH ,"photo.jpg" );
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
            mWeakReferenceHandler.sendMessage(msg);
            if (countDownTime <= 0) {
//                takePhoto();
                //倒计时结束，停止预览；
                if (mCamera != null && isTakePhoto) {
                    mCamera.stopPreview();
                }
                cancel();
                resetCountDown();
                return;
            }
            countDownTime--;
        }
    }

    ;

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
    public void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handlerThread.quit();
    }

    private final static int MSG_COUNTDOWN_SHOW = 1001;

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_COUNTDOWN_SHOW:
                        mNumtv.setVisibility(View.VISIBLE);
                        int count = (Integer) msg.obj;
                        mNumtv.setText(count + "");
                        break;
                }
            }
        };
    }
}

