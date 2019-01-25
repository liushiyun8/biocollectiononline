package com.emptech.biocollectiononline.camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.emptech.biocollectiononline.camera.utils.OpenGlUtils;
import com.emptech.biocollectiononline.camera.utils.YUVFilter;
//import com.libyuv.util.YuvUtil;

@TargetApi(21)
public class Camera2Renderer extends CameraGLRendererBase {

    public static final int YUV420P = 0;
    public static final int YUV420SP = 1;
    public static final int NV21 = 2;

    private static final String TAG = "Camera2Renderer";
    protected final String LOGTAG = "Camera2Renderer";
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private String mCameraID;
    private Size mPreviewSize = new Size(-1, -1);

    private ImageReader imageReaderPhoto;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private ByteBuffer mYBuffer;
    private ByteBuffer mUBuffer;
    private ByteBuffer mVBuffer;
    private final YUVFilter yuvFilter;
    private ImageReader.OnImageAvailableListener onImageAvailableListener=new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if(previewAndPictureCallback!=null){
                previewAndPictureCallback.onTakePhtoData(bytes,image.getWidth() ,image.getHeight() );
            }
            image.close();
        }
    };
    private ImageReader imageReader;
    private int dst_width=800;
    private int dst_height=600;

    Camera2Renderer(CameraGLSurfaceView view) {
        super(view);
        yuvFilter = new YUVFilter();
    }

    @Override
    protected void doStart() {
        Log.d(LOGTAG, "doStart");
        startBackgroundThread();
        super.doStart();
    }

    @Override
    protected void initES() {
        yuvFilter.init();
        yuvFilter.initCameraFrameBuffer(800,600 );
    }

    @Override
    protected void doStop() {
        Log.d(LOGTAG, "doStop");
        super.doStop();
        stopBackgroundThread();
    }

    boolean cacPreviewSize(final int width, final int height) {
        Log.i(LOGTAG, "cacPreviewSize: "+width+"x"+height);
        if(mCameraID == null) {
            Log.e(LOGTAG, "Camera isn't initialized!");
            return false;
        }
        CameraManager manager = (CameraManager) mView.getContext()
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager
                    .getCameraCharacteristics(mCameraID);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //对于静态图片，使用可用的最大值来拍摄。
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return o1.getWidth()-o2.getWidth();
                }
            });
            //设置ImageReader,将大小，图片格式
            imageReaderPhoto = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
            imageReaderPhoto.setOnImageAvailableListener(onImageAvailableListener,mBackgroundHandler);

            int bestWidth = 0, bestHeight = 0;
            float aspect = (float)width / height;
            for (Size psize : map.getOutputSizes(SurfaceTexture.class)) {
                int w = psize.getWidth(), h = psize.getHeight();
                Log.d(LOGTAG, "trying size: "+w+"x"+h);
                if ( width >= w && height >= h &&
                     bestWidth <= w && bestHeight <= h &&
                     Math.abs(aspect - (float)w/h) < 0.2 ) {
                    bestWidth = w;
                    bestHeight = h;
                }
            }
            Log.i(LOGTAG, "best size: "+bestWidth+"x"+bestHeight);
            bestWidth=800;bestHeight=600;
            if( bestWidth == 0 || bestHeight == 0 ||
                mPreviewSize.getWidth() == bestWidth &&
                mPreviewSize.getHeight() == bestHeight )
                return false;
            else {
                mPreviewSize = new Size(bestWidth, bestHeight);
                return true;
            }
        } catch (CameraAccessException e) {
            Log.e(LOGTAG, "cacPreviewSize - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Log.e(LOGTAG, "cacPreviewSize - Illegal Argument Exception");
        } catch (SecurityException e) {
            Log.e(LOGTAG, "cacPreviewSize - Security Exception");
        }
        return false;
    }

    @Override
    protected void openCamera(int id) {
        Log.i(LOGTAG, "openCamera");
        CameraManager manager = (CameraManager) mView.getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String camList[] = manager.getCameraIdList();
            if(camList.length == 0) {
                Log.e(LOGTAG, "Error: camera isn't detected.");
                return;
            }
            if(id == CameraRenderer.CAMERA_ID_ANY) {
                mCameraID = camList[0];
            } else {
                for (String cameraID : camList) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                    if( id == CameraRenderer.CAMERA_ID_BACK &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK ||
                        id == CameraRenderer.CAMERA_ID_FRONT &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraID = cameraID;
                        break;
                    }
                }
            }
            if(mCameraID != null) {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException(
                            "Time out waiting to lock camera opening.");
                }
                Log.i(LOGTAG, "Opening camera: " + mCameraID);
                manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            Log.e(LOGTAG, "OpenCamera - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Log.e(LOGTAG, "OpenCamera - Illegal Argument Exception");
        } catch (SecurityException e) {
            Log.e(LOGTAG, "OpenCamera - Security Exception");
        } catch (InterruptedException e) {
            Log.e(LOGTAG, "OpenCamera - Interrupted Exception");
        }
    }

    @Override
    public int drawImage() {
//        super.drawImage();
        return yuvFilter.onDrawToTexture(OpenGlUtils.NO_TEXTURE,mYBuffer,mUBuffer,mVBuffer, 600,800);
    }

    @Override
    protected void closeCamera() {
        Log.i(LOGTAG, "closeCamera");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            mCameraOpenCloseLock.release();
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }

    };

    private void createCameraPreviewSession() {
        final int w=mPreviewSize.getWidth(), h=mPreviewSize.getHeight();
        Log.i(LOGTAG, "createCameraPreviewSession("+w+"x"+h+")");
        if(w<0 || h<0)
            return;
        try {
            mCameraOpenCloseLock.acquire();
            if (null == mCameraDevice) {
                mCameraOpenCloseLock.release();
                Log.e(LOGTAG, "createCameraPreviewSession: camera isn't opened");
                return;
            }
            if (null != mCaptureSession) {
                mCameraOpenCloseLock.release();
                Log.e(LOGTAG, "createCameraPreviewSession: mCaptureSession is already started");
                return;
            }
            if(null == mSTexture) {
                mCameraOpenCloseLock.release();
                Log.e(LOGTAG, "createCameraPreviewSession: preview SurfaceTexture is null");
                return;
            }
            mSTexture.setDefaultBufferSize(w, h);

            Surface surface = new Surface(mSTexture);

            mPreviewRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            //新增加获取预览帧的接口
            imageReader = ImageReader.newInstance(800, 600, ImageFormat.YUV_420_888, 1);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    final Image image = reader.acquireNextImage();
                    mBackgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                     int width = image.getWidth();
                    int height = image.getHeight();
                    Log.e("onImageAvailable", "width:"+width+",height:"+height);
                    byte[] yuv420Datas = getBytesFromImageAsType(image, YUV420P);
//                    byte[] cropData=new byte[dst_width*dst_height*3/2];
                    byte[] dstdata=new byte[width*height*3/2];
//                    YuvUtil.cropYUV(yuv420Datas,width ,height ,cropData , dst_width, dst_height, (width-dst_width)/2,(height-dst_height)/2 );
//                    YuvUtil.MirrorYUV(cropData, dst_width, dst_height, dstdata);
//                    if(getYUVdata(dstdata,dst_width,dst_height)){
//                        mView.requestRender();
//                    }
                    byte[] nv21Data=new byte[dstdata.length];
//                    YuvUtil.yuvI420ToNV21(dstdata,nv21Data ,dst_width ,dst_height );
                    previewAndPictureCallback.onPreviewData(nv21Data, dst_width, dst_height);
                            image.close();
                        }
                    });
//
//                    image.close();
                }
            }, mBackgroundHandler);
            mPreviewRequestBuilder.addTarget(imageReader.getSurface());


            mCameraDevice.createCaptureSession(Arrays.asList(surface,imageReader.getSurface(),imageReaderPhoto.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured( CameraCaptureSession cameraCaptureSession) {
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                                int cropW=800,cropH=600;
//                                Rect zoom = new Rect(cropW, cropW, w - cropW, h - cropH);
//                                mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                                Log.i(LOGTAG, "CameraPreviewSession has been started");
                            } catch (CameraAccessException e) {
                                Log.e(LOGTAG, "createCaptureSession failed");
                            }
                            mCameraOpenCloseLock.release();
                        }

                        @Override
                        public void onConfigureFailed(
                                CameraCaptureSession cameraCaptureSession) {
                            Log.e(LOGTAG, "createCameraPreviewSession failed");
                            mCameraOpenCloseLock.release();
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(LOGTAG, "createCameraPreviewSession");
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted while createCameraPreviewSession", e);
        }
        finally {
            //mCameraOpenCloseLock.release();
        }
    }

    private boolean getYUVdata(byte[] bytes, int width, int height) {
        if(bytes.length<width*height*ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8){
            return false;
        }
        byte[] yBytes = new byte[width * height];
        //临时存储uv数据的
        byte uBytes[] = new byte[width * height / 4];
        byte vBytes[] = new byte[width * height / 4];
        int index=0;
        System.arraycopy(bytes, 0, yBytes, index, width*height);
        index+=width*height;
        System.arraycopy(bytes, index, uBytes, 0, width*height/4);
        index+=width*height/4;
        System.arraycopy(bytes, index, vBytes, 0, width*height/4);
        mYBuffer = ByteBuffer.allocateDirect(yBytes.length)
                .order(ByteOrder.nativeOrder());
        mYBuffer.put(yBytes);

        mUBuffer = ByteBuffer.allocateDirect(uBytes.length)
                .order(ByteOrder.nativeOrder());
        mUBuffer.put(uBytes);

        mVBuffer = ByteBuffer.allocateDirect(vBytes.length)
                .order(ByteOrder.nativeOrder());
        mVBuffer.put(vBytes);

        return true;
    }

    public static byte[] getBytesFromImageAsType(Image image, int type) {
        try {
            //获取源数据，如果是YUV格式的数据planes.length = 3
            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //目标数组的装填到的位置
            int dstIndex = 0;

            Log.e(TAG,"yuvBytes.length:"+yuvBytes.length+",width:"+width+",height:"+height );

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();
                Log.w(TAG,"i:"+i+",pixelsStride:"+pixelsStride+",rowStride"+rowStride );
                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }

//            image.close();

            //根据要求的结果类型进行填充
            switch (type) {
                case YUV420P:
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
                    break;
                case YUV420SP:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = uBytes[i];
                        yuvBytes[dstIndex++] = vBytes[i];
                    }
                    break;
                case NV21:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = vBytes[i];
                        yuvBytes[dstIndex++] = uBytes[i];
                    }
                    break;
            }
            return yuvBytes;
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            Log.i(TAG, e.toString());
        }
        return null;
    }

    private void startBackgroundThread() {
        Log.i(LOGTAG, "startBackgroundThread");
        stopBackgroundThread();
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.i(LOGTAG, "stopBackgroundThread");
        if(mBackgroundThread == null)
            return;
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(LOGTAG, "stopBackgroundThread");
        }
    }

    @Override
    protected void setCameraPreviewSize(int width, int height) {
        Log.i(LOGTAG, "setCameraPreviewSize("+width+"x"+height+")");
        if(mMaxCameraWidth  > 0 && mMaxCameraWidth  < width)  width  = mMaxCameraWidth;
        if(mMaxCameraHeight > 0 && mMaxCameraHeight < height) height = mMaxCameraHeight;
        try {
            mCameraOpenCloseLock.acquire();

            boolean needReconfig = cacPreviewSize(width, height);
            mCameraWidth  = mPreviewSize.getWidth();
            mCameraHeight = mPreviewSize.getHeight();

            if( !needReconfig ) {
                mCameraOpenCloseLock.release();
                return;
            }
            if (null != mCaptureSession) {
                Log.d(LOGTAG, "closing existing previewSession");
                mCaptureSession.close();
                mCaptureSession = null;
            }
            mCameraOpenCloseLock.release();
            createCameraPreviewSession();
        } catch (InterruptedException e) {
            mCameraOpenCloseLock.release();
            throw new RuntimeException("Interrupted while setCameraPreviewSize.", e);
        }
    }

    public void startPreview(){
        if(mCaptureSession!=null)
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview(){
        if(mCaptureSession!=null){
            //先停止以前的预览状态
            try {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void takePhoto(){
        try {
            // 创建一个拍照的CaptureRequest.Builder
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReaderPhoto.getSurface());

            //设置一系列的拍照参数，这里省略
            // 自动对焦
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 根据设备方向计算设置照片的方向
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);


            //先停止以前的预览状态
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();

            //执行拍照动作
            mCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


}
