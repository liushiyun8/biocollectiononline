package com.emptech.biocollectiononline.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xubaipei on 2018/3/29.
 */

public class QualityView extends View {
    Paint mPaint;

    float mRemainX = 0;

    int mColor = 0;

    int mProgressValue = 0;

    int angle = 80;

    int mFillingColor = Color.parseColor("#02c5fa");

    int mNormalColor = Color.parseColor("#c5e2f7");

    int mFullColor = Color.parseColor("#0befab");

    int mBgColor = Color.parseColor("#345274");

    int mWidth,mHeight;

    int mRadius = 5;

    Path mArrowPath;
    Path mDestPath;
    float mArrowHeight;

    public QualityView(Context context) {
        this(context,null);
    }

    public QualityView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    @SuppressLint("NewApi")
    public QualityView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,0);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mColor = Color.parseColor("#16cbf8");
        mPaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthModel == MeasureSpec.AT_MOST || heightModel == MeasureSpec.AT_MOST){
            width = 106;
            height = 22;
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mArrowSpace = 6.0f * w / 106.0f;
        mArrowHeight = 7.0f * h / 22.0f;
        mRemainX =  10.2f * w / 106.0f;
        createShape();
        mDestPath = new Path();
    }

    private boolean isAuto = true;
    public void setAuto(boolean isAuto){
        this.isAuto = isAuto;
        if(isAuto){
            postInvalidate();
        }
    }

    public void setProgress(int progress) {
        this.mProgressValue = progress;
        if(mProgressValue < 0 ){
            mProgressValue = 0 ;
        }
        postInvalidate();
    }

    public void setProgress(float progress){
        mProgressValue = (int) progress * 8;
        postInvalidate();
    }

    float mArrowSpace;
    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(1,1,mWidth - 1.0f, mHeight - 1.0f,mRadius,mRadius,mPaint);
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(1,1,mWidth - 1.0f, mHeight - 1.0f,mRadius,mRadius,mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        drawArrow(canvas);
    }

    private void createShape(){
        if (mArrowPath == null){
            mArrowPath = new Path();
        }
        double k = - Math.tan(angle/2.0d);
        //origin
        mArrowPath.moveTo(mRemainX,mHeight/2.0f - mArrowHeight);
        float x = (float)(mArrowHeight / k + mRemainX);
        mArrowPath.lineTo(x,mHeight / 2.0f);
        mArrowPath.lineTo(mRemainX,mHeight /2.0f + mArrowHeight);
        //
        mArrowPath.lineTo(mRemainX + mArrowSpace,mHeight /2.0f + mArrowHeight);
        mArrowPath.lineTo(x + mArrowSpace,mHeight / 2.0f);
        mArrowPath.lineTo(mRemainX + mArrowSpace,mHeight / 2.0f - mArrowHeight);
        mArrowPath.close();
    }

    Matrix matrix = new Matrix();

    private void drawArrow(Canvas canvas){
        for (int i = 1; i<= 8;i++){
            if (mProgressValue == 8){
                mPaint.setColor(mFullColor);
            }else if (i  > mProgressValue){
                mPaint.setColor(mNormalColor);
            }else {
                if(!isAuto){
                    mPaint.setColor(mFullColor);
                }else{
                    mPaint.setColor(mFillingColor);
                }
            }
            matrix.setTranslate(mRemainX *  (i -1),0);
            mArrowPath.transform(matrix,mDestPath);
            canvas.drawPath(mDestPath,mPaint);
        }
        if (mProgressValue == 7){
            mProgressValue =0;
        }
        if (mProgressValue < 8){
            mProgressValue++;
        }

        if(isAuto){
            postInvalidateDelayed(300);
        }
    }
}
