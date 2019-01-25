package com.emptech.biocollectiononline.manager;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.emptech.biocollectiononline.common.FaceListener;
import com.emptech.biocollectiononline.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片检测是否合格；
 */

public class CheckCameraFaceAdapter implements Camera.FaceDetectionListener {
    private final static String TAG = "CheckCameraFaceAdapter";

    private int mPreviewWidth=800;

    private int mPreviewHeight=600;

    private FaceListener mListener;

    private static String appid = "GCAjHsrJjr9WE87FehPKHhbgoj2KGeKcLHebV1SkWfhS";

    private static String ft_key = "EZfxkLhgUGZvm3img1yZQTtJJaG7qcp6TR9NCiiwex4p";

    private AFT_FSDKEngine engine = null;

    private AFT_FSDKVersion version = null;

    private int lastYCenter=-1;

    private List<Rect> historyFaces;


    private List<AFT_FSDKFace> result;
    private int lastCenter;
    private int lastDistance;

    private int lastStatus;

    public void init() {
        historyFaces=new ArrayList<>();
        engine = new AFT_FSDKEngine();
        version = new AFT_FSDKVersion();

        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(appid, ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 3);
        Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
        err = engine.AFT_FSDK_GetVersion(version);
        Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());
        //记得释放
        err = engine.AFT_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());
        // 用来存放检测到的人脸信息列表
        result = new ArrayList<>();
    }

    private int processTime = 0;

    public void process(byte[] data, int width, int height) {
        float rationX = (float) width / (float) mPreviewWidth;
        float rationY = (float) height / (float) mPreviewHeight;

        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(appid, ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 2);
        if(err.getCode()!=FACE_OK)
        LogUtils.e("com.arcsoft", "AFT_FSDK_InitialFaceEngine =" + err.getCode());
        //输入的data数据为NV21格式（如Camera里NV21格式的preview数据），其中height不能为奇数，人脸跟踪返回结果保存在result。

        err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
        if(err.getCode()!=0)
        LogUtils.e("com.arcsoft", "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
        AFT_FSDKFace minFace=null;
        for (AFT_FSDKFace face : result) {
            Log.d("com.arcsoft", "Face:" + face.toString());
            Rect rect = face.getRect();
            int left = (int) (rect.top / rationX);
            int right = (int) (rect.bottom / rationX);
            int top = (int) (rect.left / rationY);
            int bottom = (int) (rect.right / rationY);

            LogUtils.v(TAG, "rationX:" + rationX + ",rationY:" + rationY + ",left:" + left + ",top:" + top + ",right:" + right + ",bottom:" + bottom);
            int YCenter=(bottom+top)/2;
            if(minFace==null){
                minFace=face;
            }else {
                Rect rect1 = minFace.getRect();
                int top1 = (int) (rect1.left / rationY);
                int bottom1 = (int) (rect1.right / rationY);
                int YminCenter=(top1+bottom1)/2;
                if(Math.abs(YminCenter-lastYCenter)>Math.abs(YCenter-lastYCenter)){
                    minFace=face;
                }
            }
            processTime = 0;
        }
        if(minFace!=null){
            Rect rect = minFace.getRect();
            int top = (int) (rect.left / rationY);
            int bottom = (int) (rect.right / rationY);
            int left = (int) (rect.top / rationX);
            int right = (int) (rect.bottom / rationX);
            Rect rect1 = new Rect(left, top, right, bottom);
            int y = (top + bottom) / 2;
            if (mListener != null&&judgeHistory(rect1)) {
                mListener.faceArea(left, top, right, bottom);
                lastYCenter = y;
            }
            addtoHistorys(rect1);
        }
        processTime++;
        if (processTime > 3) {
            processTime = 0;
            if (mListener != null) {
                mListener.noFace();
            }
        }
       err = engine.AFT_FSDK_UninitialFaceEngine();
       result.clear();
       if(err.getCode()!=0)
       LogUtils.e("com.arcsoft", "AFT_FSDK_UninitialFaceEngine =" + err.getCode());
    }

    private boolean judgeHistory(Rect rec) {
        if(historyFaces.size()==0){
            return true;
        }
        int count=0;
        for (int i = 0; i < historyFaces.size(); i++) {
            Rect rect = historyFaces.get(i);
            if(Math.abs(rect.centerY()-rec.centerY())<100){
                count++;
            }
            if(count>=3){
                return true;
            }
        }
        return false;
    }

    private void addtoHistorys(Rect rect) {
        if(historyFaces.size()>=5){
            historyFaces.remove(0);
        }
        historyFaces.add(rect);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.e(TAG, "onFaceDetection");
        if (faces.length > 0) {
            Camera.Face face = faces[0];
            Log.e(TAG, "score:" + face.score);
            if (face.leftEye == null || face.rightEye == null || face.mouth == null) {
                return;
            }
            Log.e(TAG, "leftEyeX:" + face.leftEye.x + " leftEyeY:" + face.leftEye.y);
            Log.e(TAG, "rightEyeX:" + face.rightEye.x + " rightEyeY:" + face.rightEye.y);
            Log.e(TAG, "mouthx:" + face.mouth.x + " mouthy:" + face.mouth.y);
            Log.e(TAG, "score:" + face.score);
            Log.e(TAG, "left:" + face.rect.left + " top:" + face.rect.top + " right:" + face.rect.right + " bottom:" + face.rect.bottom);

            int cx = -face.rect.centerY();
            int cy = -face.rect.centerX();
            cx = caculateRealValue(cx, mPreviewWidth);
            cy = mPreviewHeight - caculateRealValue(cy, mPreviewHeight);

            exChangeSize(face.leftEye);
            exChangeSize(face.rightEye);
            exChangeSize(face.mouth);

            int leftEyeX = mPreviewWidth - caculateRealValue(face.leftEye.x, mPreviewWidth);
            int leftEyeY = caculateRealValue(face.leftEye.y, mPreviewHeight);
            int rightEyeX = mPreviewWidth - caculateRealValue(face.rightEye.x, mPreviewWidth);
            int rightEyeY = caculateRealValue(face.rightEye.y, mPreviewHeight);
            int mouseX = mPreviewWidth - caculateRealValue(face.mouth.x, mPreviewWidth);
            int mouseY = caculateRealValue(face.mouth.y, mPreviewHeight);

            int rectLeft = caculateRealValue(face.rect.left, mPreviewWidth);
            int rectRight = caculateRealValue(face.rect.right, mPreviewWidth);


            LogUtils.v(TAG, "RleftEyeX:" + leftEyeX + "RleftEyeY:" + leftEyeY + "RrightX:" + rightEyeX + "RrightY:" + rightEyeY);
            int distance = rectRight - rectLeft;
            LogUtils.v(TAG, "DisTance:" + distance);
            if (mListener != null) {
                mListener.faceIn(leftEyeX, leftEyeY, rightEyeX, rightEyeY, mouseX, mouseY);
                mListener.faceArea(cx - distance, cy - distance, cx + distance, cy + distance);
            }
            return;
        }
        if (mListener != null) {
            mListener.noFace();
        }
    }

    private static void exChangeSize(Point point) {
        int x = point.x;
        point.x = point.y;
        point.y = x;
    }

    private static int caculateRealValue(int value, int displayValue) {
        return (int) ((value + 1000f) / 2000f * (float) (displayValue));
    }

    public void setFaceLinstener(FaceListener listener) {
        mListener = listener;
    }


    public void setmPreviewSize(int mPreviewWidth, int mPreviewHeight) {
        this.mPreviewWidth = mPreviewWidth;
        this.mPreviewHeight = mPreviewHeight;
    }

    /**
     * 检测是否人脸是否合格；
     */

    public final static int FACE_INCLINED = 1;//脸部偏斜；
    public final static int FACE_INVERSION = 2;//脸部倒置
    public final static int FACE_BIG = 3;//脸部偏大；
    public final static int FACE_SMALL = 4;//脸部偏小；
    public final static int FACE_RIGHT = 5;//脸部偏右；
    public final static int FACE_LEFT = 6;//脸部偏左；
    public final static int FACE_DOWN = 7;//脸部偏下；
    public final static int FACE_UP = 8;//脸部偏上'
    public final static int FACE_NOCHANGE=9;//脸部跟上次没有改变；
    public final static int FACE_OK = 0; //脸部正确；

    public int canTakePhoto(int left, int top, int right, int bottom, int mPreviewWidth, int mPreviewHeight) {
        int centerX = left + (right - left) / 2;
        int centerY = top + (bottom - top) / 2;
        int distanceX = right - left;
        int distanceY = bottom - top;

        //消除位置浮动和大小的浮动
        if(Math.abs(centerY-lastCenter)<20&&Math.abs(distanceX-lastDistance)<30){
            return FACE_NOCHANGE;
        }

        //值越大，脸部判定越往下；
        if (centerY > mPreviewHeight * 0.52f) {
            if(lastStatus==FACE_DOWN){
                lastCenter=centerY;
                Log.v(TAG, "FACE_DOWN  centerY" + centerY);
                return FACE_DOWN;
            }else {
               lastStatus=FACE_DOWN;
               return FACE_NOCHANGE;
            }

        }

        if (centerY < mPreviewHeight * 0.43f) {
            if(lastStatus==FACE_UP){
                lastCenter=centerY;
                Log.v(TAG, "FACE_UP  centerY" + centerY);
                return FACE_UP;
            }else {
                lastStatus=FACE_UP;
                return FACE_NOCHANGE;
            }

        }

        if (distanceX > mPreviewWidth * 0.58f) {
            if(lastStatus==FACE_BIG){
                lastDistance=distanceX;
                Log.v(TAG, "FACE_BIG " + "distanceX:" + distanceX + "distanceY:" + distanceY);
                return FACE_BIG;
            }else {
                lastStatus=FACE_BIG;
                return FACE_NOCHANGE;
            }

        }
        Log.e(TAG, "FACE_SMALL " + "distanceX:" + distanceX + "distanceY:" + distanceY);

        if (distanceX < mPreviewWidth * 0.30f) {
            if(lastStatus==FACE_SMALL){
                lastDistance=distanceX;
                Log.v(TAG, "FACE_SMALL " + "distanceX:" + distanceX + "distanceY:" + distanceY);
                return FACE_SMALL;
            }else {
                lastStatus=FACE_SMALL;
                return FACE_NOCHANGE;
            }

        }

        if (centerX > mPreviewWidth / 2f + mPreviewWidth / 7f) {
            if(lastStatus==FACE_RIGHT){
                return FACE_RIGHT;
            }else {
                lastStatus=FACE_RIGHT;
                return FACE_NOCHANGE;
            }

        }

        if (centerX < mPreviewWidth / 2f - mPreviewWidth / 7f) {
            if(lastStatus==FACE_LEFT){
                return FACE_LEFT;
            }else {
                lastStatus=FACE_LEFT;
                return FACE_NOCHANGE;
            }
        }
        if(lastStatus==FACE_OK){
            return FACE_OK;
        }else {
            lastStatus=FACE_OK;
            return FACE_NOCHANGE;
        }
    }

    public void release(){
        historyFaces.clear();
        historyFaces=null;
    }

}
