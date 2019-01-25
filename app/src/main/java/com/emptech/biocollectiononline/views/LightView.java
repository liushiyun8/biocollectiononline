package com.emptech.biocollectiononline.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xubaipei on 2018/3/26.
 */

public class LightView extends View {
    Paint mPaint;

    float radius;

    public LightView(Context context) {
        this(context,null);
    }

    public LightView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthModel == MeasureSpec.AT_MOST || heightModel == MeasureSpec.AT_MOST){
                height = width = 250;
        }else {
            width = height = Math.min(width,height);
        }
        setMeasuredDimension(width,height);
        radius = getMeasuredWidth() / 2.0f;
        generaShader();
    }

    float[][] stops = {
//            {0.25f, 0.2f,0.3f,0.4f},
//            {0.25f, 0.5f,0.6f,0.7f},
//            {0.25f, 0.8f,0.9f,1.0f}
            {0.5f, 0.5f,0.6f,0.8f},
            {0.5f, 0.6f,0.7f,0.9f},
            {0.5f, 0.7f,0.8f,1.0f}
//            {0.3f, 0.7f,0.8f,0.9f},

    };
    int[][] colors = {
            {Color.parseColor("#33ff0000"),
                    Color.parseColor("#66000000"),
                    Color.parseColor("#88000000"),
                    Color.parseColor("#ff000000")},
//            {Color.parseColor("#99ff0000"),
//                    Color.parseColor("#66000000"),
//                    Color.parseColor("#88000000"),
//                    Color.parseColor("#ff000000")},

            {Color.parseColor("#ffff0000"),
                    Color.parseColor("#66000000"),
                    Color.parseColor("#88000000"),
                    Color.parseColor("#ff000000")},
//            {Color.parseColor("#77ff0000"),
//                    Color.parseColor("#66000000"),
//                    Color.parseColor("#88000000"),
//                    Color.parseColor("#ff000000")},
//            {Color.parseColor("#99ff0000"),
//                    Color.parseColor("#66000000"),
//                    Color.parseColor("#88000000"),
//                    Color.parseColor("#ff000000")},
//            {Color.parseColor("#bbff0000"),
//                    Color.parseColor("#66000000"),
//                    Color.parseColor("#88000000"),
//                    Color.parseColor("#ff000000")},
//            {Color.parseColor("#ddff0000"),
//                    Color.parseColor("#66000000"),
//                    Color.parseColor("#88000000"),
//                    Color.parseColor("#ff000000")},
            {Color.parseColor("#ffff0000"),
                    Color.parseColor("#66000000"),
                    Color.parseColor("#88000000"),
                    Color.parseColor("#ff000000")}
    };
    RadialGradient[] shaders = new RadialGradient[3];
    int index = 0;

    private void generaShader(){
        for (int i = 0; i  < 3;i++){
            shaders[i] =  new RadialGradient(radius,radius,radius,colors[i],stops[i], Shader.TileMode.REPEAT);
        }
    }


    boolean increase = false;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setShader(shaders[index % 3]);
        canvas.drawCircle(radius,radius,radius,mPaint);
        index ++;
        if (index == 3){
            index = 0;
            postInvalidateDelayed(2000);
        }else {
            postInvalidateDelayed(500);
        }
//        if (index == 2){
//            increase = false;
//        }
//        if (increase){
//            index++;
//        }else {
//            index--;
//        }
    }
}
