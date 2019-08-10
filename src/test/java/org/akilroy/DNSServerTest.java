package org.akilroy;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

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
        DNSHeader header = DNSServer.extractHeader(new DataInputStream(new ByteArrayInputStream(datagram)));
        assertArrayEquals(decodeHex("ffa901200001000000000001"), header.getBytes());
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
