package com.raphaelyu.phonemouse;

import java.awt.event.InputEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PressAndReleaseTest {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();

        // mouse down
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(PhoneMouseServer.PACKET_TYPE_PRESS);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(InputEvent.BUTTON1_MASK);
        byte buf[] = baos.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                PhoneMouseServer.PHONE_MOUSE_PORT);
        socket.send(packet);

        // mouse move
        for (int i = 0; i < 1; i++) {
            baos = new ByteArrayOutputStream();
            out = new DataOutputStream(baos);
            out.writeByte(PhoneMouseServer.PACKET_TYPE_MOVE);
            out.writeLong(System.currentTimeMillis());
            out.writeFloat((float) Math.random() * 2 - 1);
            out.writeFloat((float) Math.random() * 2 - 1);
            buf = baos.toByteArray();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                    PhoneMouseServer.PHONE_MOUSE_PORT);
            socket.send(packet);
        }

        // mouse up
        baos = new ByteArrayOutputStream();
        out = new DataOutputStream(baos);
        out.writeByte(PhoneMouseServer.PACKET_TYPE_RELEASE);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(InputEvent.BUTTON1_MASK);
        buf = baos.toByteArray();
        packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(),
                PhoneMouseServer.PHONE_MOUSE_PORT);
        socket.send(packet);

        socket.close();
        System.out.println("Test finished.");
    }
}
