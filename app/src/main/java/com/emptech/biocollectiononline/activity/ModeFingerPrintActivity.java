package com.emptech.biocollectiononline.activity;


import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.FingerFragment;

/**
 * Created by linxiaohui on 2018/1/4.
 */

public class ModeFingerPrintActivity extends ModePreviewActivity {
    @Override
    protected BaseFragment setFragment() {
        return new FingerFragment();
    }

    @Override
        public void requestFinishFragmentListener() {
//        startActivity(WaitingActivity.class);
        finish();
    }

    @Override
    public int getTitleString() {
        return R.string.fingerprint_title;
    }
}
