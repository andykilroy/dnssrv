package org.akilroy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collections;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Kilroy
 */
public class DNSServerTest
{
    private final DNSServer server = new DNSServer(4001);

    @Before
    public void setUp() throws IOException
    {
        server.setResolver(new HashMapResolver(Collections.singletonMap("www.cloudflare.com", new InetAddress[]{
            Inet4Address.getByName("104.17.209.9"),
            Inet4Address.getByName("104.17.210.9")
        })));
    }

    @Test
    public void extractHeaderAlways12Bytes() throws Exception
    {
        byte[] datagram = decodeHex("ffa901200001000000000001037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000");
        DNSHeader header = DNSServer.extractHeader(wrappedBuffer(datagram));
        assertArrayEquals(decodeHex("ffa901200001000000000001"), header.getBytes());
    }

    @Test
    public void handleRequestWithOneQuestion_norecord() throws Exception
    {
        ByteBuf output = Unpooled.buffer(8192);
        server.setResolver(Resolver.EMPTY);
        server.handleQuery(
            new DNSHeader(decodeHex("ffa901200001000000000001")),
            wrappedBuffer(decodeHex("037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000")),
            output
        );
        assertArrayEquals(
            decodeHex("ffa981030000000000000000"
            ),
            toByteArray(output));
    }

    @Test
    public void handleRequestWithOneQuestion_expectOneAnswer() throws Exception
    {
        ByteBuf output = Unpooled.buffer(8192);
        server.handleQuery(
            new DNSHeader(decodeHex("ffa901200001000000000001")),
            wrappedBuffer(decodeHex("037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000")),
            output
        );
        assertArrayEquals(
            decodeHex("ffa981000001000200000000" + // header
                      "037777770a636c6f7564666c61726503636f6d0000010001" + // question
                      "037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d109" + // answer 1
                      "037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d209"   // answer 2
            ),
            toByteArray(output));
    }

    @Test
    public void handleRequestWithOneQuestion_expectTwoAnswers() throws Exception
    {
        ByteBuf output = Unpooled.buffer(8192);
        server.handleQuery(
            new DNSHeader(decodeHex("ffa901200001000000000001")),
            wrappedBuffer(decodeHex("037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000")),
            output
            );
        assertArrayEquals(
            decodeHex("ffa981000001000200000000" + // header
                      "037777770a636c6f7564666c61726503636f6d0000010001" + // question
                      "037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d109" + // answer 1
                      "037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d209"   // answer 2
                      ),
            toByteArray(output));
    }

    @Test
    public void handleQuestion() throws Exception
    {
        ByteBuf output = Unpooled.buffer(8192);
        server.handleQuestion(
            wrappedBuffer(decodeHex("037777770a636c6f7564666c61726503636f6d0000010001")),
            output
        );
        assertArrayEquals(
            decodeHex("037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d109" +
                      "037777770a636c6f7564666c61726503636f6d00000100010000012c00046811d209"),
            toByteArray(output));
    }

    private byte[] toByteArray(ByteBuf output)
    {
        byte[] bytes = new byte[output.readableBytes()];
        output.readBytes(bytes);
        return bytes;
    }

    private byte[] decodeHex(String input)
    {
        return HexCoder.decode(input);
    }

    private byte[] copyLeadingBytes(byte[] header, int length)
    {
        byte[] expectedHeaderBytes = new byte[length];
        System.arraycopy(header, 0, expectedHeaderBytes, 0, length);
        return expectedHeaderBytes;
    }
}
