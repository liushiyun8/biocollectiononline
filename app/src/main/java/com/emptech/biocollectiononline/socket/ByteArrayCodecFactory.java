package com.emptech.biocollectiononline.socket;

import com.emptech.biocollectiononline.AppConfig;
import com.emptech.biocollectiononline.utils.LogUtils;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by linxiaohui on 2017/12/29.
 */

public class ByteArrayCodecFactory implements ProtocolCodecFactory {
    private ByteArrayDecoder decoder;
    private ByteArrayEncoder encoder;

    public ByteArrayCodecFactory() {
        encoder = new ByteArrayEncoder();
        decoder = new ByteArrayDecoder();
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }

    //编码
    public class ByteArrayEncoder extends ProtocolEncoderAdapter {

        @Override
        public void encode(IoSession session, Object message,
                           ProtocolEncoderOutput out) {
            try {
                IoBuffer buffer = IoBuffer.allocate(((byte[]) message).length);
                buffer.setAutoExpand(true);
                buffer.setAutoShrink(true);
                buffer.put((byte[]) message);
                buffer.flip();
                out.write(buffer);
                LogUtils.v(AppConfig.MODULE_SERVER, "发送长度：" + ((byte[]) message).length);
                out.flush();
                buffer.free();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 这种方法的返回值是重点：
     * <p>
     * 1、当内容刚好时，返回false，告知父类接收下一批内容
     * <p>
     * 2、内容不够时须要下一批发过来的内容，此时返回false，这样父类 CumulativeProtocolDecoder
     * <p>
     * 会将内容放进IoSession中，等下次来数据后就自己主动拼装再交给本类的doDecode
     * <p>
     * 3、当内容多时，返回true，由于须要再将本批数据进行读取。父类会将剩余的数据再次推送本
     * <p>
     * 类的doDecode
     */
    public class ByteArrayDecoder extends CumulativeProtocolDecoder {
        private int headLen = 18;

        @Override
        protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {


            if (ioBuffer.remaining() > headLen) {//有数据时候，获取包头数据
                //标记当前position的快照标记mark，以便后继的reset操作能恢复position位置
                ioBuffer.mark();
                byte[] head = new byte[headLen];
                ioBuffer.get(head);
                if(!(head[0]==MessageConfig.STARTFRAME[0]&&head[1]== MessageConfig.STARTFRAME[1])){
                    ioBuffer.reset();
                    ioBuffer.get();
                    return true;
                }
                int calcDataLength = ((head[2] & 0xff) << 24)
                        + ((head[3] & 0xff) << 16) + ((head[4] & 0xff) << 8)
                        + (head[5] & 0xff);

                //注意上面的get操作会导致下面的remaining()值发生变化
                int recSize = ioBuffer.remaining();
                LogUtils.v(AppConfig.MODULE_SERVER, "解析包体长度:" + calcDataLength + ";当前存在:" + recSize);
                if ((recSize) < calcDataLength+2) {
                    //如果消息内容不够，则重置恢复position位置到操作前,进入下一轮, 接收新数据，以拼凑成完整数据
                    ioBuffer.reset();
                    return false;
                } else {
                    //消息内容足够
                    ioBuffer.reset();//重置恢复position位置到操作前
                    int sumlen = headLen + calcDataLength + 2;//总长 = 包头+包体+CRC校验
                    byte[] packArr = new byte[sumlen];
                    IoBuffer buffer = IoBuffer.allocate(sumlen)
                            .setAutoExpand(true);
                    LogUtils.v(AppConfig.MODULE_SERVER, "接收完整包长度:" + sumlen);
                    ioBuffer.get(packArr, 0, sumlen);

                    buffer.put(packArr);
                    buffer.flip();
                    protocolDecoderOutput.write(buffer);
                    buffer.free();

                    if (ioBuffer.remaining() > 0) {//如果读取一个完整包内容后还粘了包，就让父类再调用一次，进行下一次解析
                        LogUtils.v(AppConfig.MODULE_SERVER, "包接收完了！");
                        return true;
                    }
                }
            }
            return false;//处理成功，让父类进行接收下个包
        }
    }
}
