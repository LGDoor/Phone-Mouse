package com.raphaelyu.phonemouse;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PhoneMouseServer {

    final static short PHONE_MOUSE_PORT = 5329;
    final static int MAX_PACKET_LENGTH = 32;
    final static byte PACKET_TYPE_DISCOVER = 0x1;
    final static byte PACKET_TYPE_REPLY = 0x2;
    final static byte PACKET_TYPE_MOVE = 0x10;

    private static final PrintWriter STD_OUT = new PrintWriter(System.out, true);

    private BlockingQueue<Motion> moves = new ArrayBlockingQueue<Motion>(2000);
    private Robot mRobot;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mMaxMotionDist;

    private class Motion {

        public Motion(long timestamp, float x, float y) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
        }

        public long timestamp;
        public float x;
        public float y;
    }

    private class SocketThread extends Thread {
        private DatagramChannel mServerChannel;
        private ByteBuffer buffer;

        public SocketThread() throws IOException {
            super("Receiver");
            mServerChannel = DatagramChannel.open();
            mServerChannel.socket().bind(new InetSocketAddress(PHONE_MOUSE_PORT));
            buffer = ByteBuffer.allocate(MAX_PACKET_LENGTH);
        }

        /**
         * Receive the Discover packets and reply.
         */
        @Override
        public void run() {
            while (true) {
                buffer.clear();
                SocketAddress addr;
                try {
                    addr = mServerChannel.receive(buffer);
                    buffer.flip();
                    if (buffer.limit() > 0) {
                        byte type = buffer.get();
                        STD_OUT.print("received from: " + addr.toString());
                        switch (type) {
                        case PACKET_TYPE_DISCOVER:
                            STD_OUT.println(", type: DISCOVER");
                            buffer.clear();
                            buffer.put(PACKET_TYPE_REPLY);
                            buffer.flip();
                            try {
                                mServerChannel.send(buffer, addr);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;
                        case PACKET_TYPE_MOVE:
                            STD_OUT.println(", type: MOVE");
                            long timestamp = buffer.getLong();
                            float moveX = buffer.getFloat();
                            float moveY = buffer.getFloat();
                            Motion motion = new Motion(timestamp, moveX, moveY);
                            moves.offer(motion);
                            break;
                        default:
                            // otherwise ignore the packet.
                            STD_OUT.println(", type: UNKNOWN, " + type);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }   // end of while(true)
        }
    }

    private void init() throws AWTException {
        mRobot = new Robot();
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        mScreenHeight = scrSize.height;
        mScreenWidth = scrSize.width;

        // 取屏幕长宽的最小值
        mMaxMotionDist = (mScreenHeight < mScreenWidth) ? mScreenHeight : mScreenWidth;
    }

    public void start() throws InterruptedException, AWTException, IOException {
        init();
        SocketThread th = new SocketThread();
        th.start();
        while (true) {
            long lastTime = 0l;
            Motion motion = moves.take();
            if (motion.timestamp > lastTime) {
                PointerInfo info = MouseInfo.getPointerInfo();
                if (info != null) {
                    Point point = info.getLocation();
                    mRobot.mouseMove((int) (motion.x * mMaxMotionDist) + point.x,
                            (int) (motion.y * mMaxMotionDist) + point.y);
                }
                lastTime = motion.timestamp;
            }
        }
    }

    /**
     * @param args
     * @throws InterruptedException
     * @throws AWTException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, AWTException, IOException {
        PhoneMouseServer server = new PhoneMouseServer();
        server.start();
    }
}
