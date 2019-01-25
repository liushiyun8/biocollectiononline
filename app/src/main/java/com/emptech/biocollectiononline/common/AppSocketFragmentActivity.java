package com.emptech.biocollectiononline.common;

import android.os.Bundle;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.fragment.BaseSocketFragment;
import com.emptech.biocollectiononline.socket.message.SocketSession;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public abstract class AppSocketFragmentActivity extends AppFragmentActivity {
    protected SocketSession mSocketSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setSocketSession(SocketSession mSocketSession) {
        this.mSocketSession = mSocketSession;
        if (currentFragment instanceof BaseSocketFragment) {
            ((BaseSocketFragment) currentFragment).setSocketSession(mSocketSession);
            if(AppConfig.isTestSocket)
            LogUtils.e(TAG, "session send to Fragment：" + currentFragment.getClass().getSimpleName());
        }
    }


}
