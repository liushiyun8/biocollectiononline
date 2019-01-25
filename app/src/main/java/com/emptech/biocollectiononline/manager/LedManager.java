package com.emptech.biocollectiononline.manager;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.common.App;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LedManager {

    public static void open(){
        open(getCurrentValue());
    }

    public static int open(int value){
        int ret=-1;
        DataOutputStream stream=null;
        try {
            stream= new DataOutputStream(new FileOutputStream(new File("/sys/class/leds/light/brightness")));
            stream.writeBytes(String.valueOf(value));
            stream.flush();
            ret=0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(stream!=null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public static int getCurrentValue(){
        return App.get().getmPreferencesManager().getIntegerPref(AppConfig.PREFERENCE_KEY_FILLLIGHT,255);
    }

    public static void saveCurrentValue(int value){
        App.get().getmPreferencesManager().setIntegerPref(AppConfig.PREFERENCE_KEY_FILLLIGHT, value);
    }


    public static void close(){
        open(0);
//        openGPIO(0);
    }


    public static int openGPIO(int value){
        int ret=-1;
        DataOutputStream stream=null;
        try {
            stream= new DataOutputStream(new FileOutputStream(new File("/sys/xd_gpios/led/GPIO_LED")));
            stream.writeBytes(String.valueOf(value));
            stream.flush();
            ret=0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(stream!=null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
}
