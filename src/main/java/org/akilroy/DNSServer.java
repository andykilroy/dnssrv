package org.akilroy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * @author Andrew Kilroy
 */
public class DNSServer
{
    private static final byte END_OF_QNAME = (byte) 0x0;
    private static final int RR_TYPE_A = 1;
    private static final int RR_CLASS_IN = 1;

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
        ByteBuf questions = Unpooled.buffer(512);
        ByteBuf answers = Unpooled.buffer(512);

        instream.markReaderIndex();
        int questionBytes = handleQuestion(instream, answers);
        instream.resetReaderIndex();
        instream.readBytes(questions, questionBytes);

        outstream
            .writeShort(header.getID())
            .writeShort(0)
            .writeShort(1)
            .writeShort(2)
            .writeShort(0)
            .writeShort(0);
        outstream
            .writeBytes(questions)
            .writeBytes(answers);

    }

    public int handleQuestion(ByteBuf instream, ByteBuf outstream) throws IOException
    {
        int start = instream.readerIndex();
        int qnamelength = instream.bytesBefore(END_OF_QNAME);
        ByteBuf qnamebytes = instream.readBytes(qnamelength + 1);
        int qtype = instream.readShort();
        int qclass = instream.readShort();
        int questionLength = instream.readerIndex() - start;
        InetAddress[] addrs = lookup(qnamebytes.slice());
        for (InetAddress addr : addrs)
        {
            outstream.writeBytes(qnamebytes.slice());
            outstream.writeShort(RR_TYPE_A);   // 2
            outstream.writeShort(RR_CLASS_IN); // 2
            outstream.writeInt(300);           // 4
            outstream.writeShort(4);           // 2
            outstream.writeBytes(addr.getAddress()); // 4
        }
        return questionLength;
    }

    private InetAddress[] lookup(ByteBuf qnamebytes) throws UnknownHostException
    {
        return new InetAddress[]{
            Inet4Address.getByName("104.17.209.9"),
            Inet4Address.getByName("104.17.210.9")
        };
    }

    private byte[] toBytes(ByteBuf buf)
    {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes.length);
        return bytes;
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
