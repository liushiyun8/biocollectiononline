package com.emptech.biocollectiononline.utils;

import android.widget.Toast;

import com.emptech.biocollectiononline.common.App;

public class UiUtils {

    public static void shortTip(int strId){
        Toast.makeText(App.mContext, strId,Toast.LENGTH_SHORT ).show();
    }

    public static void shortTip(String str){
        Toast.makeText(App.mContext, str,Toast.LENGTH_SHORT ).show();
    }

    public static void longTip(int strId){
        Toast.makeText(App.mContext, strId,Toast.LENGTH_LONG).show();
    }
}
