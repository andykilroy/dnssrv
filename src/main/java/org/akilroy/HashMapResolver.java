package org.akilroy;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Kilroy
 */
public class HashMapResolver implements Resolver
{
    private final HashMap<String, InetAddress[]> map;

    public HashMapResolver(Map<String, InetAddress[]> map)
    {
        this.map = new HashMap(map);
    }

    @Override
    public InetAddress[] lookup(String qname)
    {
        return map.get(qname);
    }
}
