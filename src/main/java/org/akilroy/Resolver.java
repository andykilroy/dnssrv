package org.akilroy;

import java.net.InetAddress;

/**
 * @author Andrew Kilroy
 */
public interface Resolver
{
    public InetAddress[] lookup(String qname);

    Resolver EMPTY = new Resolver()
    {
        @Override
        public InetAddress[] lookup(String qname)
        {
            return null;
        }
    };
}
