package com.raphaelyu.airmouse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.raphaelyu.airmouse.Server;

public class MoveTest {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        for (int i = 0; i < 10000; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeByte(Server.PACKET_TYPE_MOVE);
            out.writeLong(System.currentTimeMillis());
            out.writeFloat((float) Math.random() * 2 - 1);
            out.writeFloat((float) Math.random() * 2 - 1);
            byte buf[]=baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                    Server.SERVER_PORT);
            socket.send(packet);
        }
        socket.close();
        System.out.println("Test finished.");
    }
}
