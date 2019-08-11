A simple DNS server.  [RFC 1035](https://www.ietf.org/rfc/rfc1035.txt)
was used as a reference.

This implementation makes a few assumptions:

   1. The only incoming query is for a host address translation
      (requests of type 'A')
   1. The only requested class is for the Internet (class 'IN')

Other requests would be ignored/rejected.
