package org.akilroy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Kilroy
 */
public class HexCoderTest
{
    @Test
    public void encodeBytesLessThan128() throws Exception
    {
        assertEquals("000102", HexCoder.hexEncode(new byte[] {0x00, 0x01, 0x02}, 3));
        assertEquals("0d0e0f", HexCoder.hexEncode(new byte[] {0x0d, 0x0e, 0x0f}, 3));
        assertEquals("707172", HexCoder.hexEncode(new byte[] {0x70, 0x71, 0x72}, 3));
        assertEquals("7d7e7f", HexCoder.hexEncode(new byte[] {0x7d, 0x7e, 0x7f}, 3));
    }

    @Test
    public void encodeBytesGreaterThanEqualTo128Unsigned() throws Exception
    {
        assertEquals("808182", HexCoder.hexEncode(new byte[] {-128, -127, -126}, 3));
        assertEquals("8d8e8f", HexCoder.hexEncode(new byte[] {-128 + 13, -128 + 14, -128 + 15}, 3));
        assertEquals("f0f1f2", HexCoder.hexEncode(new byte[] {-16 + 0, -16 + 1, -16 + 2}, 3));
        assertEquals("fdfeff", HexCoder.hexEncode(new byte[] {-3, -2, -1}, 3));
    }

}
