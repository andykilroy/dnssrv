package org.akilroy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

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

    private void handleDatagram(DatagramSocket socket, DatagramPacket packet) throws IOException
    {
        System.out.println(HexCoder.encode(packet.getData(), packet.getLength()));
        DataInputStream instream = new DataInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
        DNSHeader header = extractHeader(instream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outstream = new DataOutputStream(out);

        if (header.isQuery())
        {
            handleQuery(header, instream, outstream);
            respond(socket, packet.getSocketAddress(), out.toByteArray());
        }
        // otherwise it's a response, so ignore it.

    }

    public void handleQuery(DNSHeader header, DataInputStream instream, DataOutputStream outstream)
    {

    }

    private void respond(DatagramSocket socket, SocketAddress respondTo, byte[] data) throws IOException
    {
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, respondTo);
        socket.send(packet);
    }

    public static DNSHeader extractHeader(DataInputStream datastream) throws IOException
    {
        byte[] array = new byte[12];
        readN(datastream, array, 0, 12);
        return new DNSHeader(array);
    }

    private static void readN(DataInputStream stream, byte[] output, int offset, int length) throws IOException
    {
        int readCount = stream.read(output, offset, length);
        while (readCount < length)
        {
            int readBytes = length - readCount;
            int off = readCount;
            readCount += stream.read(output, off, readBytes);
        }
    }

}
