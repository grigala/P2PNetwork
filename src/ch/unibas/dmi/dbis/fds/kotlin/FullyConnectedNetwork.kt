package ch.unibas.dmi.dbis.fds.kotlin

class FullyConnectedNetwork(numberOfBits: Int) : Network(numberOfBits) {

    override fun arrangeOverlayStructure() {
        for (p1 in nodes.values) {
            for (p2 in nodes.values) {
                if (p1 !== p2)
                    p1.addConnection(p2.nodeID)
            }
        }
    }

    override fun createPeer(id: String): PeerNode {
        return FullyConnectedPeer(this, id)
    }
}
