package org.akilroy;

/**
 * @author Andrew Kilroy
 */
public class DNSHeader
{
    private final byte[] inputBytes;

    public DNSHeader(byte[] headerBytes)
    {
        this.inputBytes = headerBytes;
    }

    public boolean isQuery()
    {
        int comp = unsignedInt(inputBytes[2]) & 0x80;
        return comp != 0x80;
    }

    private int unsignedInt(byte inputByte)
    {
        return 0xff & (int)inputByte;
    }

    public byte[] getBytes()
    {
        return inputBytes;
    }
}
