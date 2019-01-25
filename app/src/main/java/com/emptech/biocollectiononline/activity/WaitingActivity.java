package com.emptech.biocollectiononline.activity;

import android.os.Bundle;

import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.common.AppSocketFragmentActivity;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.WaitingFragment;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class WaitingActivity extends AppSocketFragmentActivity {
    @Override
    protected BaseFragment setFragment() {
        return new WaitingFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.get().getActivityCount() > 2) {
            LogUtils.v(TAG,"当前界面太多，不进行等待");
            finish();
        }
    }

    @Override
    protected void setupTopBar() {
        hideActionBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
