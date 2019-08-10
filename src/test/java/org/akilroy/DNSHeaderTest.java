package org.akilroy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrew Kilroy
 */
public class DNSHeaderTest
{
    @Test
    public void readHeaderID() throws Exception
    {
        DNSHeader header = new DNSHeader(HexCoder.decode("ffa901200001000000000001"));
        assertEquals(0xffa9, header.getID());
    }
    @Test
    public void QRBitOff_IndicatesIsQuery() throws Exception
    {
        DNSHeader header = new DNSHeader(HexCoder.decode("ffa901200001000000000001"));
        assertTrue(header.isQuery());
    }

    @Test
    public void QRBitOn_IndicatesIsNotQuery() throws Exception
    {
        DNSHeader header = new DNSHeader(HexCoder.decode("ffa981200001000000000001"));
        assertFalse(header.isQuery());
    }
}
