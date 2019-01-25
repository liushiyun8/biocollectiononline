package com.emptech.biocollectiononline.common;

/**
 * Created by linxiaohui on 2017/11/22.
 */

public abstract class AppFullActivity extends AppActivity {


    @Override
    protected void setupTopBar() {
        hideActionBar();
    }

    @Override
    public int getTitleString() {
        return 0;
    }


}
