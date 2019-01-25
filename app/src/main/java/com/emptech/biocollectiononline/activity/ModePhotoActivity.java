package com.emptech.biocollectiononline.activity;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.CameraFragment;
/**
 * Created by linxiaohui on 2018/1/4.
 */

public class ModePhotoActivity extends ModePreviewActivity {

    @Override
    protected BaseFragment setFragment() {
        return new CameraFragment();
    }

    @Override
    public int getTitleString() {
        return 0;
    }

    @Override
    protected void setupTopBar() {
        hideActionBar();
    }

    @Override
    public void requestFinishFragmentListener() {
        finish();
    }
}
