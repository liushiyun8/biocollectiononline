package com.emptech.biocollectiononline.camera.utils;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.emptech.biocollectiononline.R;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class YUVFilter extends GPUImageFilter {
    private float[] mTextureTransformMatrix;
    private int mTextureTransformMatrixLocation;
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

    private int[] mFrameBuffers = null;
    private int[] mFrameBufferTextures = null;
    private int mFrameWidth = -1;
    private int mFrameHeight = -1;
    private int m_textureUniformY;
    private int m_textureUniformU;
    private int m_textureUniformV;

    public YUVFilter(){
        super(OpenGlUtils.readShaderFromRawResource(R.raw.default_vertex) ,
                OpenGlUtils.readShaderFromRawResource(R.raw.yuv_fragment));
    }

    protected void onInit() {
        super.onInit();
//        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(mGLProgId, "textureTransform");
//        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
//        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        //获取片源着色器源码中的变量,用于纹理渲染
        m_textureUniformY = GLES20.glGetUniformLocation(mGLProgId, "tex_y");
        m_textureUniformU = GLES20.glGetUniformLocation(mGLProgId, "tex_u");
        m_textureUniformV = GLES20.glGetUniformLocation(mGLProgId, "tex_v");
        Log.e("TAG","m_textureUniformY:"+m_textureUniformY +
             ",m_textureUniformU:"+m_textureUniformU+",m_textureUniformV:"+m_textureUniformV);
        checkGlError("glGetUniformLocation");
//        setBeautyLevel(0);
    }

    public void setTextureTransformMatrix(float[] mtx){
        mTextureTransformMatrix = mtx;
    }

    public int onDrawFrame(int textureId, ByteBuffer yPlan, ByteBuffer uPlan, ByteBuffer vPlan, int width, int height) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if(!isInitialized()) {
            return OpenGlUtils.NOT_INIT;
        }
        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
//        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        int yTex = OpenGlUtils.loadTexture1(yPlan, width, height, OpenGlUtils.NO_TEXTURE);
        int uTex=OpenGlUtils.loadTexture1(uPlan, width/2,height/2 ,OpenGlUtils.NO_TEXTURE );
        int vTex=OpenGlUtils.loadTexture1(vPlan, width/2,height/2 ,OpenGlUtils.NO_TEXTURE );


        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTex);
        GLES20.glUniform1i(m_textureUniformY, 0);

        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uTex);
        GLES20.glUniform1i(m_textureUniformU, 1);

        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vTex);
        GLES20.glUniform1i(m_textureUniformV, 2);

        if(textureId != OpenGlUtils.NO_TEXTURE){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return OpenGlUtils.ON_DRAWN;
    }

    @Override
    public int onDrawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if(!isInitialized()) {
            return OpenGlUtils.NOT_INIT;
        }
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        if(textureId != OpenGlUtils.NO_TEXTURE){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return OpenGlUtils.ON_DRAWN;
    }

    public int onDrawToTexture(int textureId, ByteBuffer yPlan, ByteBuffer uPlan, ByteBuffer vPlan, int width, int height) {
        if(mFrameBuffers == null)
            return OpenGlUtils.NO_TEXTURE;
        runPendingOnDrawTasks();
        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glUseProgram(mGLProgId);
        if(!isInitialized()) {
            return OpenGlUtils.NOT_INIT;
        }
        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
//        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

//        if(textureId != OpenGlUtils.NO_TEXTURE){
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//            GLES20.glUniform1i(mGLUniformTexture, 0);
//        }

        int yTex = OpenGlUtils.loadTexture1(yPlan, width, height, OpenGlUtils.NO_TEXTURE);
        int uTex=OpenGlUtils.loadTexture1(uPlan, width/2,height/2 ,OpenGlUtils.NO_TEXTURE );
        int vTex=OpenGlUtils.loadTexture1(vPlan, width/2,height/2 ,OpenGlUtils.NO_TEXTURE );
        checkGlError("loadTexture1");


        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTex);
        GLES20.glUniform1i(m_textureUniformY, 0);
        checkGlError("m_textureUniformY:"+m_textureUniformY);

        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uTex);
        GLES20.glUniform1i(m_textureUniformU, 1);
        checkGlError("m_textureUniformU");

        //选择当前活跃的纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        //允许建立一个绑定到目标纹理的有名称的纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vTex);
        GLES20.glUniform1i(m_textureUniformV, 2);
        checkGlError("m_textureUniformV");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        Log.e("YUVFilter", "画完了");
//        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        return mFrameBufferTextures[0];
    }

    public void initCameraFrameBuffer(int width, int height) {
        if(mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height))
            destroyFramebuffers();
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];

            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            GLES20.glGenTextures(1, mFrameBufferTextures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    private void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                    Log.e("error","***** " + op + ": glError " + error, null);
                    throw new RuntimeException(op + ": glError " + error);
                }
         }

    public void destroyFramebuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;
        mFrameHeight = -1;
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public void setBeautyLevel(int level){
        switch (level) {
            case 0:
                setFloat(mParamsLocation, 0.0f);
                break;
            case 1:
                setFloat(mParamsLocation, 1.0f);
                break;
            case 2:
                setFloat(mParamsLocation, 0.8f);
                break;
            case 3:
                setFloat(mParamsLocation,0.6f);
                break;
            case 4:
                setFloat(mParamsLocation, 0.4f);
                break;
            case 5:
                setFloat(mParamsLocation,0.33f);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFramebuffers();
    }
}
