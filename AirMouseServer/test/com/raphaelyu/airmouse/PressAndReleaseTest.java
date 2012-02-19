package com.raphaelyu.airmouse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.raphaelyu.airmouse.Server;

public class PressAndReleaseTest {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        // left button down
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(Server.PACKET_TYPE_PRESS);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(Server.PACKET_MOUSE_BUTTON_LEFT);
        byte buf[] = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                Server.SERVER_PORT);
        socket.send(packet);

        // mouse move
        for (int i = 0; i < 1; i++) {
            baos = new ByteArrayOutputStream();
            out = new DataOutputStream(baos);
            out.writeByte(Server.PACKET_TYPE_MOVE);
            out.writeLong(System.currentTimeMillis());
            out.writeFloat((float) Math.random() * 2 - 1);
            out.writeFloat((float) Math.random() * 2 - 1);
            buf = baos.toByteArray();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                    Server.SERVER_PORT);
            socket.send(packet);
        }

        // left button up
        baos = new ByteArrayOutputStream();
        out = new DataOutputStream(baos);
        out.writeByte(Server.PACKET_TYPE_RELEASE);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(Server.PACKET_MOUSE_BUTTON_LEFT);
        buf = baos.toByteArray();
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                Server.SERVER_PORT);
        socket.send(packet);

        // right button up
        baos = new ByteArrayOutputStream();
        out = new DataOutputStream(baos);
        out.writeByte(Server.PACKET_TYPE_PRESS);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(Server.PACKET_MOUSE_BUTTON_RIGHT);
        buf = baos.toByteArray();
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                Server.SERVER_PORT);
        socket.send(packet);

        // left button up
        baos = new ByteArrayOutputStream();
        out = new DataOutputStream(baos);
        out.writeByte(Server.PACKET_TYPE_RELEASE);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(Server.PACKET_MOUSE_BUTTON_RIGHT);
        buf = baos.toByteArray();
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                Server.SERVER_PORT);
        socket.send(packet);

        socket.close();
        System.out.println("Test finished.");
    }
}
