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
        int comp = unsignedByte(2) & 0x80;
        return comp != 0x80;
    }

    public byte[] getBytes()
    {
        return inputBytes;
    }

    public int getID()
    {
        return unsignedBEShort(0);
    }

    private int unsignedByte(int index)
    {
        return 0xff & (int)inputBytes[index];
    }

    private int unsignedBEShort(int index)
    {
        int hi = unsignedByte(index);
        int lo = unsignedByte(index + 1);
        return (hi << 8) | lo;
    }

}
