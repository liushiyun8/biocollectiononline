package com.emptech.biocollectiononline.manager;

import android.app.Activity;

import com.emptech.biocollectiononline.socket.message.SocketSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linxiaohui on 2018/1/8.
 */

public class SocketSessionManager {
    private Map<Class<? extends Activity>, SocketSession> mClientMap = new HashMap<>();

    private static class INSTANCE {
        private static final SocketSessionManager INSTANCE = new SocketSessionManager();
    }

    public static SocketSessionManager getInstance() {
        return INSTANCE.INSTANCE;
    }

    public SocketSession getAndRemoveSocketSession(Class<? extends Activity> Clazz) {
        SocketSession client = mClientMap.get(Clazz);
        mClientMap.remove(Clazz);
        return client;
    }

    public void putSocketSession(Class<? extends Activity> Clazz, SocketSession socketSession) {
        mClientMap.put(Clazz, socketSession);
    }
}
