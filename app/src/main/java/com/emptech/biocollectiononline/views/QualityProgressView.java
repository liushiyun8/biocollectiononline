package com.emptech.biocollectiononline.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xubaipei on 2018/3/29.
 */

public class QualityProgressView extends View {
    Paint mPaint;

    int mRadius = 5;

    int mProgressCircleRadius = 10;

    int mCircleSpace = 0;

    int mColor = 0;

    int mProgressValue = 2;

    int mFillingColor = Color.parseColor("#02c5fa");

    int mNormalColor = Color.parseColor("#c5e2f7");

    int mFullColor = Color.parseColor("#0befab");

    int mWidth,mHeight;


    public QualityProgressView(Context context) {
        this(context,null);
    }

    public QualityProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    @SuppressLint("NewApi")
    public QualityProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
            width = 152;
            height = 35;
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mProgressCircleRadius = (int)(4.0f * h / 14.0f);
        mCircleSpace = (int) (15.0f * w / 152.0f);
        mWidth = w;
        mHeight = h;
    }

    public void setProgress(int progress) {
        this.mProgressValue = progress;
        invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mColor);
        canvas.drawRoundRect(0,0,mWidth - 1.0f, mHeight - 1.0f,mRadius,mRadius,mPaint);
        drawArrow(canvas);
        //drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas){
        float centerX = mWidth / 2.0f;
        float centerY = mHeight / 2.0f;
        mPaint.setStyle(Paint.Style.FILL);
        float firstCenter = centerX - 2 * mCircleSpace - 4 * mRadius;
        mPaint.setColor(mFillingColor);

        for (int i =0; i< 5;i ++){
            if (i> mProgressValue){
                mPaint.setColor(mNormalColor);
            }
            // Draw circle
            canvas.drawCircle(firstCenter + i * (2 * mRadius + mCircleSpace),centerY,mProgressCircleRadius,mPaint);
        }
        mPaint.setStyle(Paint.Style.STROKE);
    }

    Path mArrowPath;
    private void initPath(){
        if (mArrowPath == null){
            mArrowPath = new Path();
        }

    }

    private void drawArrow(Canvas canvas){

    }
}
