package com.emptech.biocollectiononline.manager;

/**
 * Created by linxiaohui on 2018/1/10.
 */

public class OperationThread extends Thread {
    private boolean mCanceled = false;

    public OperationThread() {
    }

    public boolean IsCanceled() {
        return mCanceled;
    }

    public void Cancel() {
        mCanceled = true;
        try {
            this.join();    //5sec timeout
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
