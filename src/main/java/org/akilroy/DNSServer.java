package org.akilroy;

import com.sun.org.apache.xpath.internal.operations.String;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
        ByteBuf instream = Unpooled.wrappedBuffer(packet.getData(), 0, packet.getLength()).asReadOnly();
        DNSHeader header = extractHeader(instream);

        if (header.isQuery())
        {
            ByteBuf output = Unpooled.buffer(8192);
            handleQuery(header, instream, output);
            respond(socket, packet.getSocketAddress(), output);
        }
        // otherwise it's a response, so ignore it.

    }

    public void handleQuery(DNSHeader header, ByteBuf instream, ByteBuf outstream) throws IOException
    {
        ByteBuf reply = Unpooled.buffer(8192);
        handleQuestion(instream, reply);
    }

    private void handleQuestion(ByteBuf instream, ByteBuf outstream) throws IOException
    {
    }

    private void respond(DatagramSocket socket, SocketAddress respondTo, ByteBuf bytes) throws IOException
    {
        byte[] response = new byte[bytes.readableBytes()];
        bytes.readBytes(response);

        DatagramPacket packet = new DatagramPacket(response, 0, response.length, respondTo);
        socket.send(packet);
    }

    public static DNSHeader extractHeader(ByteBuf datastream) throws IOException
    {
        byte[] array = new byte[12];
        datastream.readBytes(array);
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
