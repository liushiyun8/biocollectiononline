package com.emptech.biocollectiononline.activity;

import android.os.Bundle;
import android.os.Message;

import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.common.AppFullActivity;
import com.emptech.biocollectiononline.manager.WeakReferenceHandler;

/**
 * Created by linxiaohui on 2018/1/21.
 */

public class CollectionFinishActivity extends AppFullActivity {
    private final int FINISH_ACITITY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MusicPlayer.get(this).play(R.raw.finishcollection, false);
        mWeakReferenceHandler.sendEmptyMessageDelayed(FINISH_ACITITY, 3000);
    }

    @Override
    protected WeakReferenceHandler.MyHandleMessage setHandlerMessage() {
        return new WeakReferenceHandler.MyHandleMessage() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FINISH_ACITITY:
                        if(!isFinishing())
                        App.get().finishAllActivitys();
//                        startActivity(WaitingActivity.class);
                        break;
                }
            }
        };
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_finish;
    }
}
