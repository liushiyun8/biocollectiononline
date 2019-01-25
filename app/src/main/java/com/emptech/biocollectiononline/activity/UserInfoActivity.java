package com.emptech.biocollectiononline.activity;

import android.os.Bundle;

import com.emptech.biocollectiononline.common.AppSocketFragmentActivity;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.UserInfoFragment;
import com.emptech.biocollectiononline.socket.message.SessionUserInfoSocket;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class UserInfoActivity extends AppSocketFragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected BaseFragment setFragment() {
//        MusicPlayer.get(this).play(R.raw.userinfoconfirm, false);
        return new UserInfoFragment();
    }

    @Subscribe
    public void onEventMainThread(SessionUserInfoSocket sessionSocket) {
        if (sessionSocket != null) {
            LogUtils.v(TAG, "接收到采集信号，传输给Fragment");
            setSocketSession(sessionSocket);
        }
    }

    @Override
    public void requestFinishFragmentListener() {
        startActivity(WaitingActivity.class);
        finish();
    }
}
