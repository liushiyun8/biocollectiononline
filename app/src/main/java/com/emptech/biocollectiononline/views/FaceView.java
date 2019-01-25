package com.emptech.biocollectiononline.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.emptech.biocollectiononline.R;

/**
 * Created by xubaipei on 2018/1/8.
 */


public class FaceView extends View {
    Paint mPaint;

    Path mPath;

    int left,top,right,bottom;

    int mLEyeX,mLEyeY,mREyeX,mREyeY,mMouseX,mMouseY;

    int mDirtion = 0;

    public static final int UP = 0x01;
    public static final int DOWN = 0x02;
    public static final int LEFT = 0x03;
    public static final int RIGHT = 0x04;
    public static final int CENTER = 0x05;
    private Bitmap redBitmap;
    private Bitmap greenBitmap;
    private Bitmap whiteBitmap;
    private Rect srcRect;
    private Rect dstRect;
    private int status=-1;

    public FaceView(Context context) {
        super(context);
        init();
    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setFaceArea(int left,int top,int right,int bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        invalidate();
    }

    public void setFace(int leftEyeX,int leftEyeY,int rightEyeX,int rightEyeY,int mouseX,int mouseY ){
        mLEyeX = leftEyeX;
        mLEyeY = leftEyeY;
        mREyeX = rightEyeX;
        mREyeY = rightEyeY;
        mMouseX = mouseX;
        mMouseY = mouseY;
        invalidate();
    }

    public void setDirtion(int dirtion){
        mDirtion = dirtion;
        invalidate();
    }


    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(2);
        redBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.red);
        greenBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.green);
        whiteBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.white);
        srcRect = new Rect(0, 0, redBitmap.getWidth(), redBitmap.getHeight());
        dstRect = new Rect(0, 0, 600, 800);
        mPath = new Path();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (status){
            case -1:
                canvas.drawBitmap(whiteBitmap,srcRect, dstRect,mPaint);
                break;
            case 0:
                canvas.drawBitmap(greenBitmap,srcRect, dstRect,mPaint);
                break;
            case 1:
                canvas.drawBitmap(redBitmap,srcRect, dstRect,mPaint);
                break;
        }
//        canvas.drawArc(100,150 ,500 ,550 ,0 ,-180 , false, mPaint);
        if (right - left >0) {
//            mPaint.setColor(Color.parseColor("#eeeeee"));
//            canvas.drawRect(left,top,right,bottom,mPaint);
//            canvas.drawCircle(left + (right -left) / 2f, top + (bottom - top) /2f, (right - left) /2f, mPaint);
//            mPaint.setColor(Color.RED);
//            canvas.drawCircle(mLEyeX, mLEyeY, 10, mPaint);
//            mPaint.setColor(Color.YELLOW);
//            canvas.drawCircle(mREyeX, mREyeY, 10, mPaint);
//            mPaint.setColor(Color.GREEN);
//            canvas.drawCircle(mMouseX, mMouseY, 10, mPaint);

            drawDir(canvas,(int)( left + (right -left) / 2f), (int)(top + (bottom - top) /2f),(int) ((right - left) /2f));
        }
    }

    private void drawDir(Canvas canvas,int centerX,int centerY,int radius){
        mPath.reset();
        mPaint.setColor(Color.GREEN);
        switch (mDirtion) {
            case UP:
                mPath.moveTo(centerX, centerY - radius - 0.5f * radius);
                mPath.lineTo(centerX - 0.15f * radius, centerY - radius - 0.2f * radius);
                mPath.moveTo(centerX, centerY - radius - 0.5f * radius);
                mPath.lineTo(centerX + 0.15f * radius, centerY - radius - 0.2f * radius);
                break;
            case DOWN:
                mPath.moveTo(centerX, centerY + radius + 0.5f * radius);
                mPath.lineTo(centerX - 0.15f * radius, centerY + radius + 0.2f * radius);
                mPath.moveTo(centerX, centerY + radius + 0.5f * radius);
                mPath.lineTo(centerX + 0.15f * radius, centerY + radius + 0.2f * radius);
                break;
            case LEFT:
                mPath.moveTo(centerX - radius - 0.5f * radius, centerY);
                mPath.lineTo(centerX - radius - 0.2f * radius, centerY - 0.15f * radius);
                mPath.moveTo(centerX - radius - 0.5f * radius, centerY);
                mPath.lineTo(centerX - radius - 0.2f * radius, centerY + 0.15f * radius);
                break;
            case RIGHT:
                mPath.moveTo(centerX + radius + 0.5f * radius, centerY);
                mPath.lineTo(centerX + radius + 0.2f * radius, centerY - 0.15f * radius);
                mPath.moveTo(centerX + radius + 0.5f * radius, centerY);
                mPath.lineTo(centerX + radius + 0.2f * radius, centerY + 0.15f * radius);
                break;
            default:
                break;
        }
        canvas.drawPath(mPath,mPaint);
        mDirtion = CENTER;
    }

    public void setStatus(int status) {
        if(this.status!=status){
            this.status=status;
            invalidate();
        }
    }
}