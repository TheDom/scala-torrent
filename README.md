scala-torrent
=============

As a personal challenge I decided to create a [BitTorrent](http://www.bittorrent.com) client in [Scala](http://www.scala-lang.org). I chose this subject for two primary reasons:

* To explore new topics and problems aside from the iOS and web development projects I'm usually doing
* To gain more experience with [Akka](http://akka.io), as the subject is a good fit for the [actor model](http://en.wikipedia.org/wiki/Actor_model)

To make things more interesting, I do intend to only use the specification of the protocol and shall not study any existing implementation in any language.

* http://www.bittorrent.org/beps/bep_0003.html
* http://jonas.nitro.dk/bittorrent/bittorrent-rfc.html
* https://wiki.theory.org/BitTorrentSpecification

### Progress

* [✔] CLI
* [✔] Bencode parsing and encoding
* [✔] Communication with tracker
* [✔] Modelling and (un)marshalling of the Peer Wire Protocol messages
* [✔] Connection and handshake with peers
* [TODO] File exchange
* [TODO] Actor supervision
* [TODO] Actor testing

At this point I am not planning on implementing extensions to the protocol such as DHT.