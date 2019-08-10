package org.akilroy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Kilroy
 */
public class DNSServerTest
{
    @Test
    public void extractHeaderAlways12Bytes() throws Exception
    {
        byte[] datagram = decodeHex("ffa901200001000000000001037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000");
        DNSHeader header = DNSServer.extractHeader(wrappedBuffer(datagram));
        assertArrayEquals(decodeHex("ffa901200001000000000001"), header.getBytes());
    }

    @Test
    public void handleRequest() throws Exception
    {
        DNSServer server = new DNSServer(4001);
        ByteBuf output = Unpooled.buffer(8192);
        server.handleQuery(
            new DNSHeader(decodeHex("ffa901200001000000000001")),
            wrappedBuffer(decodeHex("037777770a636c6f7564666c61726503636f6d00000100010000291000000000000000")),
            output
            );
        assertArrayEquals(
            decodeHex("ffa981a00001000200000001037777770a636c6f7564666c61726503636f6d0000010001c00c000100010000012c00046811d109c00c000100010000012c00046811d2090000291000000000000000"),
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
