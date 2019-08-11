package org.akilroy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
    private static final int NAME_ERROR = 3;

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
        try
        {
            DNSResponseBuilder builder = new DNSResponseBuilder(header);
            instream.markReaderIndex();
            int questionBytes = handleQuestion(instream, builder);
            instream.resetReaderIndex();
            builder.appendQuestion(instream.readBytes(questionBytes));

            builder.writeResponse(outstream);
        }
        catch (UnknownQNameException ex)
        {
            handleUnknownQName(header, outstream);
        }
    }

    private void handleUnknownQName(DNSHeader header, ByteBuf outstream)
    {
        DNSResponseBuilder builder = new DNSResponseBuilder(header);
        builder.setRCode(NAME_ERROR);
        builder.writeResponse(outstream);
    }

    private int handleQuestion(ByteBuf instream, DNSResponseBuilder builder) throws IOException, UnknownQNameException
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
            builder.appendAnswer(qnamebytes.slice(), RR_TYPE_A, RR_CLASS_IN, 300, addr.getAddress());
        }
        return questionLength;
    }

    private InetAddress[] lookup(ByteBuf qnamebytes) throws UnknownQNameException
    {
        String name = nameAsString(qnamebytes);
        InetAddress[] lookup = resolver.lookup(name);
        if (lookup == null || lookup.length == 0) throw new UnknownQNameException("could not resolve " + name);
        return lookup;
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
        buf.readBytes(bytes);
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
