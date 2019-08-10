package org.akilroy;

/**
 * @author Andrew Kilroy
 */
public class HexCoder
{
    private static final char[] table =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
         'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encode(byte[] data, int length)
    {
        StringBuilder writer = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            writer.append(hexEncodeByte(data[i]));
        }
        return writer.toString();
    }

    private static String hexEncodeByte(byte datum)
    {
        char hi = hiNibble(datum);
        char lo = loNibble(datum);
        return new String(new char[]{hi, lo});
    }

    private static char loNibble(byte datum)
    {
        int index = 0x0f & datum;
        return table[index];
    }

    private static char hiNibble(byte datum)
    {
        int hi = 0xff & (int)datum;
        hi >>= 4;
        return table[hi];
    }

    public static byte[] decode(String hex)
    {
        if (hex.length() % 2 != 0) throw new IllegalArgumentException("all hex strings should have a length that is a multiple of 2");
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++)
        {
            int index = i * 2;
            byte b = charsToByte(hex.charAt(index), hex.charAt(index + 1));
            out[i] = b;
        }
        return out;
    }

    private static byte charsToByte(char hichar, char lochar)
    {
        byte hi = charAsByte(hichar);
        byte lo = charAsByte(lochar);
        byte b = (byte)(hi << 4);
        return (byte)(b | lo);
    }

    private static byte charAsByte(char ch)
    {
        switch (ch)
        {
            case '0': return 0x0;
            case '1': return 0x1;
            case '2': return 0x2;
            case '3': return 0x3;
            case '4': return 0x4;
            case '5': return 0x5;
            case '6': return 0x6;
            case '7': return 0x7;
            case '8': return 0x8;
            case '9': return 0x9;
            case 'a': return 0xa;
            case 'b': return 0xb;
            case 'c': return 0xc;
            case 'd': return 0xd;
            case 'e': return 0xe;
            case 'f': return 0xf;
            default: throw new IllegalArgumentException("unexpected char '" + ch + "', the char should match the regex [0-9a-z]");
        }
    }
}
