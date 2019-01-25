package com.emptech.biocollectiononline.common;

/**
 * Created by linxiaohui on 2018/1/11.
 */

public interface FaceListener {
    void faceIn(int leftEyeX, int leftEyeY, int rightEyeX, int rightEyeY, int mouseX, int mouseY);

    void faceArea(int left, int top, int right, int bottom);

    void noFace();
}
