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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author Andrew Kilroy
 */
public class DNSServer
{
    private static final byte END_OF_QNAME = (byte) 0x0;
    private static final int RR_TYPE_A = 1;
    private static final int RR_CLASS_IN = 1;
    private static final int OPS_QR_RESPONSE = 0x80;
    private static final int OPS_DR_RESPONSE = 0x01;

    private int port;
    private Resolver resolver = Resolver.EMPTY;

    public static void main(String[] args) throws Exception
    {
        DNSServer server = new DNSServer(4001);

        HashMap<String, InetAddress[]> map = new HashMap<>();
        map.put("www.cloudflare.com", new InetAddress[]{
            Inet4Address.getByName("104.17.209.9"),
            Inet4Address.getByName("104.17.210.9")
        });
        server.setResolver(new HashMapResolver(map));

        server.start();
    }

    public DNSServer(int listenPort)
    {
        port = listenPort;
    }

    public void setResolver(Resolver r)
    {
        resolver = r;
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

        // TODO need a means of recording the number of answers,
        // and writing a new header.
        outstream
            .writeShort(header.getID())
            .writeByte(OPS_DR_RESPONSE | OPS_QR_RESPONSE)
            .writeByte(0)
            .writeShort(1) // number of questions
            .writeShort(2) // number of answers
            .writeShort(0) // number of name server records
            .writeShort(0);// number of additional records
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
        String name = nameAsString(qnamebytes);
        return resolver.lookup(name);
    }

    private String nameAsString(ByteBuf qnamebytes)
    {
        StringBuilder builder = new StringBuilder();
        int len = unsignedByte(qnamebytes);
        while (len > 0)
        {
            CharSequence labelbuf = qnamebytes.readCharSequence(len, StandardCharsets.ISO_8859_1);
            builder.append(labelbuf).append('.');
            len = unsignedByte(qnamebytes);
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private int unsignedByte(ByteBuf bytes)
    {
        return 0xff & (int)bytes.readByte();
    }

    private byte[] toByteArray(ByteBuf buf)
    {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes.length);
        return bytes;
    }

    private void respond(DatagramSocket socket, SocketAddress respondTo, ByteBuf message) throws IOException
    {
        byte[] response = toByteArray(message);

        DatagramPacket packet = new DatagramPacket(response, 0, response.length, respondTo);
        socket.send(packet);
    }

    public static DNSHeader extractHeader(ByteBuf datastream) throws IOException
    {
        byte[] array = new byte[12];
        datastream.readBytes(array);
        return new DNSHeader(array);
    }

}
