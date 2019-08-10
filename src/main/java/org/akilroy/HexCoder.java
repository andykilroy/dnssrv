package org.akilroy;

/**
 * @author Andrew Kilroy
 */
public class HexCoder
{
    private static final char[] table =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
         'a', 'b', 'c', 'd', 'e', 'f'};

    public static String hexEncode(byte[] data, int length)
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
}
