package org.akilroy;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Kilroy
 */
public class HexCoderTest
{
    @Test
    public void encodeBytesLessThan128() throws Exception
    {
        assertEquals("000102", HexCoder.encode(new byte[] {0x00, 0x01, 0x02}, 3));
        assertEquals("0d0e0f", HexCoder.encode(new byte[] {0x0d, 0x0e, 0x0f}, 3));
        assertEquals("707172", HexCoder.encode(new byte[] {0x70, 0x71, 0x72}, 3));
        assertEquals("7d7e7f", HexCoder.encode(new byte[] {0x7d, 0x7e, 0x7f}, 3));
    }

    @Test
    public void encodeBytesGreaterThanEqualTo128Unsigned() throws Exception
    {
        assertEquals("808182", HexCoder.encode(new byte[] {-128, -127, -126}, 3));
        assertEquals("8d8e8f", HexCoder.encode(new byte[] {-128 + 13, -128 + 14, -128 + 15}, 3));
        assertEquals("f0f1f2", HexCoder.encode(new byte[] {-16 + 0, -16 + 1, -16 + 2}, 3));
        assertEquals("fdfeff", HexCoder.encode(new byte[] {-3, -2, -1}, 3));
    }

    @Test
    public void decodeBytes() throws Exception
    {
        byte[] ar1 = {0x00, 0x01, 0x02};
        byte[] ar2 = {0x0d, 0x0e, 0x0f};
        byte[] ar3 = {0x70, 0x71, 0x72};
        byte[] ar4 = {0x7d, 0x7e, 0x7f};
        byte[] ar5 = {-128, -127, -126};
        byte[] ar6 = {-128 + 13, -128 + 14, -128 + 15};
        byte[] ar7 = {-16 + 0, -16 + 1, -16 + 2};
        byte[] ar8 = {-3, -2, -1};

        assertArrayEquals(ar1, HexCoder.decode(HexCoder.encode(ar1, 3)));
        assertArrayEquals(ar2, HexCoder.decode(HexCoder.encode(ar2, 3)));
        assertArrayEquals(ar3, HexCoder.decode(HexCoder.encode(ar3, 3)));
        assertArrayEquals(ar4, HexCoder.decode(HexCoder.encode(ar4, 3)));
        assertArrayEquals(ar5, HexCoder.decode(HexCoder.encode(ar5, 3)));
        assertArrayEquals(ar6, HexCoder.decode(HexCoder.encode(ar6, 3)));
        assertArrayEquals(ar7, HexCoder.decode(HexCoder.encode(ar7, 3)));
        assertArrayEquals(ar8, HexCoder.decode(HexCoder.encode(ar8, 3)));
    }
}
