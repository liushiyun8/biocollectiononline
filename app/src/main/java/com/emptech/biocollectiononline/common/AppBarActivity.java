package com.emptech.biocollectiononline.common;

import android.os.Bundle;


public abstract class AppBarActivity extends AppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        WindowUtils.setNavigateEnable(this, false);
    }

    @Override
    public int getTitleString() {
        return 0;
    }
}
