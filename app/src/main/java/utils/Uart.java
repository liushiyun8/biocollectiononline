
package utils;

/**
 * Created by yzq on 2016-11-21. please the package is : package utils;
 */

public class Uart {
    // fcntl.h
    public static final int O_ACCMODE = 00000003;
    public static final int O_RDONLY = 00000000;
    public static final int O_WRONLY = 00000001;
    public static final int O_RDWR = 00000002;//可读可写
    public static final int O_NONBLOCK = 00004000;

    public static native int open(String pathname, int flags);

    public static native int close(int fd);

    public static native int set_port(int fd, int nSpeed, int nBits, byte nEvent, int nStop);

    // termbits.h
    public static final int TCIFLUSH = 0;
    public static final int TCOFLUSH = 1;
    public static final int TCIOFLUSH = 2;

    public static native int tcflush(int fd, int queue_selector);

    public static native int read(int fd, byte[] buf, int offset, int count);

    public static native int poll_read(int fd, byte[] buf, int offset, int count, int timeout_ms);

    public static native int write(int fd, byte[] buf, int offset, int count);

    //string.h
    public static native String strerror();

    static {
        System.loadLibrary("jniUart");
    }
}
