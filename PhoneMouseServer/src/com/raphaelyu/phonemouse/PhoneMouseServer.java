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

    final static boolean DEBUG_MODE = true;

    /* 协议常量 */
    final static short PHONE_MOUSE_PORT = 5329;
    final static int MAX_PACKET_LENGTH = 64;
    final static byte PACKET_TYPE_DISCOVER = 0x1;
    final static byte PACKET_TYPE_REPLY = 0x2;
    final static byte PACKET_TYPE_MOVE = 0x10;
    final static byte PACKET_TYPE_PRESS = 0x11;
    final static byte PACKET_TYPE_RELEASE = 0x12;

    private static final PrintWriter STD_OUT = new PrintWriter(System.out, true);

    private EventQueue events = EventQueue.getInstance();
    private Robot mRobot;
    private int mMaxMotionDist;

    /**
     * 
     * 网络线程，接收并解析数据包，然后将其加入事件队列
     * 
     * @author LGDoor
     */
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
                        printLog("received from: " + addr.toString());
                        switch (type) {
                        case PACKET_TYPE_DISCOVER:
                            printlnLog(", type: DISCOVER");
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
                        case PACKET_TYPE_MOVE: {
                            printlnLog(", type: MOVE");
                            long timestamp = buffer.getLong();
                            float moveX = buffer.getFloat();
                            float moveY = buffer.getFloat();
                            MouseEvent event = MouseEvent.createMoveEvent(timestamp, moveX, moveY);
                            events.offer(event);
                            break;
                        }
                        case PACKET_TYPE_PRESS: {
                            printlnLog(", type: PRESS");
                            long timestamp = buffer.getLong();
                            int button = buffer.getInt();
                            MouseEvent event = MouseEvent.createPressEvent(timestamp, button);
                            events.offer(event);
                            break;
                        }
                        case PACKET_TYPE_RELEASE: {
                            printlnLog(", type: RELEASE");
                            long timestamp = buffer.getLong();
                            int button = buffer.getInt();
                            MouseEvent event = MouseEvent.createReleaseEvent(timestamp, button);
                            events.offer(event);
                            break;
                        }
                        default:
                            // otherwise ignore the packet.
                            printlnLog(", type: UNKNOWN, " + type);
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
        int screenHeight = scrSize.height;
        int screenWidth = scrSize.width;

        // 取屏幕长宽的最小值
        mMaxMotionDist = (screenHeight < screenWidth) ? screenHeight : screenWidth;
    }

    public void start() throws InterruptedException, AWTException, IOException {
        init();

        SocketThread th = new SocketThread();
        th.start();

        // 循环等待并处理接收到的事件
        while (true) {
            long lastTime = 0l;
            MouseEvent event = events.take();
            if (event.timestamp > lastTime) {
                lastTime = event.timestamp;

                switch (event.type) {
                case MouseEvent.TYPE_MOVE:
                    PointerInfo info = MouseInfo.getPointerInfo();
                    if (info != null) {
                        Point point = info.getLocation();
                        mRobot.mouseMove((int) (event.x * mMaxMotionDist) + point.x,
                                (int) (event.y * mMaxMotionDist) + point.y);
                    }
                    break;
                case MouseEvent.TYPE_PRESS:
                    mRobot.mousePress(event.button);
                    break;
                case MouseEvent.TYPE_RELEASE:
                    mRobot.mouseRelease(event.button);
                    break;
                }
            }
        }
    }

    private void printLog(String str) {
        if (DEBUG_MODE) {
            STD_OUT.print(str);
        }
    }

    private void printlnLog(String str) {
        if (DEBUG_MODE) {
            STD_OUT.println(str);
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
