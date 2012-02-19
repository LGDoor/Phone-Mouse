package com.raphaelyu.airmouse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

import com.raphaelyu.airmouse.Server;

public class DiscoverTest {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[2];
        InetAddress broadcastIP = Inet4Address.getByName("255.255.255.255");

        for (int i = 0; i < 10; i++) {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastIP,
                    Server.SERVER_PORT);
            buf[0] = Server.PACKET_TYPE_DISCOVER;
            buf[1] = 1;
            socket.send(packet);
            socket.receive(packet);
            if (packet.getLength() == 1) {
                byte[] buffer = packet.getData();
                if (buffer[0] != 0x2) {
                    throw new Error();
                }
            } else {
                throw new Error();
            }
            socket.close();
        }
        System.out.println("Test finished.");
    }

}
