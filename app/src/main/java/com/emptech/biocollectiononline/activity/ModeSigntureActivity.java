package com.emptech.biocollectiononline.activity;

import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.fragment.BaseSocketFragment;
import com.emptech.biocollectiononline.fragment.SigntureFragment;
/**
 * Created by linxiaohui on 2018/1/4.
 */

public class ModeSigntureActivity extends ModePreviewActivity {
    @Override
    protected BaseSocketFragment setFragment() {
//        MusicPlayer.get(this).play(R.raw.signture, false);
//        LogUtils.e(TAG,"setFragment" );
        return new SigntureFragment();
    }

    @Override
    public void requestFinishFragmentListener() {
        super.requestFinishFragmentListener();
        finish();
    }

    @Override
    public int getTitleString() {
        return R.string.signature_title;
    }

}
