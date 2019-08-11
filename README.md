A simple DNS server.  [RFC 1035](https://www.ietf.org/rfc/rfc1035.txt)
was used as a reference.

This implementation makes a few assumptions:

   1. The only incoming query is for a host address translation
      (requests of type 'A')
   1. The only requested class is for the Internet (class 'IN')
   1. There is only one question in the request.

Other requests would be ignored/rejected.
