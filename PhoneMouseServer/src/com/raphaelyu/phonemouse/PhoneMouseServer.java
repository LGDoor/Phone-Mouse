package com.raphaelyu.phonemouse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class PhoneMouseServer {

    final static short PHONE_MOUSE_PORT = 5329;
    final static int MAX_PACKET_LENGTH = 32;
    final static byte PACKET_TYPE_DISCOVER = 0x1;
    final static byte PACKET_TYPE_REPLY = 0x2;
    final static byte PACKET_TYPE_MOVE = 0x3;

    private static final PrintWriter STD_OUT = new PrintWriter(System.out, true);
    private static final PrintWriter STD_ERR = new PrintWriter(System.err, true);

    private class BroadcastReceiverThread extends Thread {
        private DatagramSocket broadcastSocket;
        private byte[] buffer;

        public BroadcastReceiverThread() throws SocketException {
            super("Broadcast Receiver");
            broadcastSocket = new DatagramSocket(PHONE_MOUSE_PORT);
            buffer = new byte[MAX_PACKET_LENGTH];
        }

        /**
         * Receive the Discover packets and reply.
         */
        @Override
        public void run() {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    while (true) {
                        broadcastSocket.receive(packet);
                        if (packet.getLength() == 1) {
                            SocketAddress addr = packet.getSocketAddress();
                            STD_OUT.print("broadcast received from: " + addr.toString());
                            byte type = buffer[0];
                            if (type == PACKET_TYPE_DISCOVER) {
                                STD_OUT.println(", type: DISCOVER");
                                buffer[0] = PACKET_TYPE_REPLY;
                                DatagramPacket replyPacket = new DatagramPacket(buffer, 1, addr);
                                broadcastSocket.send(replyPacket);
                            } else {
                                // otherwise ignore the packet.
                                STD_OUT.println(", type: UNKNOWN, " + type);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }	// end of while(true)
        }
    }

    public void start() throws SocketException, InterruptedException {
        BroadcastReceiverThread th = new BroadcastReceiverThread();
        th.start();
        th.join();
    }

    /**
     * @param args
     * @throws SocketException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws SocketException, InterruptedException {
        PhoneMouseServer server = new PhoneMouseServer();
        server.start();
    }
}
