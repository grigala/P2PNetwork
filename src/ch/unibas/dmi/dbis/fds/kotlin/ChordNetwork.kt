package ch.unibas.dmi.dbis.fds.kotlin

class ChordNetwork(numberOfBits: Int) : Network(numberOfBits) {

    override fun arrangeOverlayStructure() {
        /* This method does not do anything, as Chord nodes keep the network structure intact automatically. */
    }

    override fun createPeer(id: String): PeerNode {
        return ChordPeerImpl(this, id, false)
    }
}
