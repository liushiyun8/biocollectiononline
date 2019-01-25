package com.emptech.biocollectiononline.HttpServer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.emptech.biocollectiononline.utils.LogUtils;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.filter.HttpCacheFilter;
import com.yanzhenjie.andserver.website.AssetsWebsite;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;

public class MyHttpService extends Service {
    public static final String TAG="MyHttpSERVER";

    private Server server;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e(TAG,"oncreat" );
        server = AndServer.serverBuilder().port(8080).filter(new HttpCacheFilter()).registerHandler("/getname", new RequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                LogUtils.e(TAG,request.getAllHeaders().toString() );
            }
        }).website(new AssetsWebsite(getAssets(),"web")).listener(new Server.ServerListener() {
            @Override
            public void onStarted() {
                LogUtils.e(TAG,"start");
            }

            @Override
            public void onStopped() {
                LogUtils.e(TAG,"stop");
            }

            @Override
            public void onError(Exception e) {
                LogUtils.e(TAG,"error");
            }
        }).build();

        server.startup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e(TAG,"onDestroy" );
        server.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
