package com.emptech.biocollectiononline.socket;

import android.content.Context;
import android.os.Handler;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by linxiaohui on 2017/12/29.
 */

public class SocketServer {
    private NioSocketAcceptor acceptor;
    private final int BOTH_IDLE = 5;

    public SocketServer() {
    }

    public boolean initSocket(Context context, Handler handler) {
        boolean isSuccess = false;
        try {
            acceptor = new NioSocketAcceptor();
            acceptor.setHandler(new SocketServerHandler(context,handler));
            int receiveSize = acceptor.getSessionConfig().getReceiveBufferSize();
            LogUtils.v(AppConfig.MODULE_SERVER, "初始化服务端接收最大长度：" + receiveSize);
            acceptor.getFilterChain().addLast("logger", new LoggingFilter());
            ProtocolCodecFilter fileter = new ProtocolCodecFilter(new ByteArrayCodecFactory());
            acceptor.getFilterChain().addLast("codec", fileter);
//            acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
//            acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new WebSocketCodecFactory()));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, BOTH_IDLE);
            acceptor.setReuseAddress(true);
            InetSocketAddress mInetSocketAddress = new InetSocketAddress(9898);
            acceptor.bind(mInetSocketAddress);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public void closeServer() {
        acceptor.unbind();
        acceptor.dispose();
    }


}
