package com.emptech.biocollectiononline.activity;

import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.SelectPhotoFragment;

/**
 * Created by linxiaohui on 2018/1/15.
 */

public class SelectPhotoActivity extends ModePreviewActivity {
    @Override
    protected BaseFragment setFragment() {
        return new SelectPhotoFragment();
    }

    @Override
    public void requestFinishFragmentListener() {
        super.requestFinishFragmentListener();
        startActivity(UserInfoConfirmActivity.class);
        finish();
    }
}
