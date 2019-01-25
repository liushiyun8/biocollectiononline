package com.emptech.biocollectiononline.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.emptech.biocollectiononline.socket.SocketServer;
import com.emptech.biocollectiononline.utils.LogUtils;

/**
 * Created by linxiaohui on 2018/1/3.
 */

public class ServerService extends Service {
    SocketServer mSocketServer;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("ServerService","onCreate" );
        final Handler handler = new Handler();
        mSocketServer = new SocketServer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocketServer.initSocket(ServerService.this,handler);
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("ServerService","onDestroy" );
        if (mSocketServer != null) {
            mSocketServer.closeServer();
        }
    }
}
