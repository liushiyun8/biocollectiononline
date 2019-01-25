package com.emptech.biocollectiononline.camera;

public interface PreviewAndPictureCallback {
    public void onPreviewData(byte[] data,int width,int height);
    public void onTakePhtoData(byte[] data,int width,int height);
}
