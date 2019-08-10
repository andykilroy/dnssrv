package org.akilroy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;

/**
 * @author Andrew Kilroy
 */
public class DNSServer
{
    private int port;

    public static void main(String[] args) throws Exception
    {
        DNSServer server = new DNSServer(4001);
        server.start();
    }

    public DNSServer(int listenPort)
    {
        port = listenPort;
    }

    private void start() throws Exception
    {
        DatagramSocket socket = new DatagramSocket(port);
        try
        {
            receiveLoop(socket);
        }
        finally
        {
            socket.close();
        }
    }

    private void receiveLoop(DatagramSocket socket) throws IOException
    {
        byte[] recvBuffer = new byte[1 << 16];

        for (;;)
        {
            DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
            socket.receive(packet);
            handleDatagram(socket, packet);
        }
    }

    private void handleDatagram(DatagramSocket socket, DatagramPacket packet)
    {
        System.out.println(HexCoder.hexEncode(packet.getData(), packet.getLength()));
        System.out.println(new String(packet.getData(), 0, packet.getLength(), Charset.forName("ISO-8859-1")));
    }

}
