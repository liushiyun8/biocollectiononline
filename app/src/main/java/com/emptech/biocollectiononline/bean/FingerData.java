package com.emptech.biocollectiononline.bean;

public class FingerData {
    private int width;
    private int height;
    private byte stutas;
    private int nNFIQ;
    private byte[] previewData;
    private byte[] templateData;

    public FingerData(int width, int height, byte stutas, int nNFIQ, byte[] previewData, byte[] templateData) {
        this.width = width;
        this.height = height;
        this.stutas = stutas;
        this.nNFIQ = nNFIQ;
        this.previewData = previewData;
        this.templateData = templateData;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte getStutas() {
        return stutas;
    }

    public void setStutas(byte stutas) {
        this.stutas = stutas;
    }

    public int getnNFIQ() {
        return nNFIQ;
    }

    public void setnNFIQ(int nNFIQ) {
        this.nNFIQ = nNFIQ;
    }

    public byte[] getPreviewData() {
        return previewData;
    }

    public void setPreviewData(byte[] previewData) {
        this.previewData = previewData;
    }

    public byte[] getTemplateData() {
        return templateData;
    }

    public void setTemplateData(byte[] templateData) {
        this.templateData = templateData;
    }
}
