package com.emptech.biocollectiononline.activity;


import com.emptech.biocollectiononline.R;
import com.emptech.biocollectiononline.common.App;
import com.emptech.biocollectiononline.fragment.BaseFragment;
import com.emptech.biocollectiononline.fragment.UserInfoShowFragment;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class UserInfoConfirmActivity extends ModePreviewActivity {


    @Override
    protected BaseFragment setFragment() {
//        MusicPlayer.get(this).play(R.raw.userinfoconfirm, false);
        return new UserInfoShowFragment();
    }

    @Override
    public int getTitleString() {
        return R.string.confirm_title;
    }

    @Override
    public void requestFinishFragmentListener() {
        App.get().finishAllActivitys();
        startActivity(CollectionFinishActivity.class);
    }
}
