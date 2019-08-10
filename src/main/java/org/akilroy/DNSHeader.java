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

    public boolean hasQuery()
    {
        return false;
    }

    public byte[] getBytes()
    {
        return inputBytes;
    }
}
