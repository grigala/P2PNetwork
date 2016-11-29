package ch.unibas.dmi.dbis.fds.kotlin


abstract class ChordPeerNode(network: Network, nodeID: String) : PeerNode(network, nodeID) {

    val m: Int
    val n: Long

    init {
        this.m = network.numberOfBits
        this.n = network.hash(nodeID)
    }

    abstract fun fixFingers(fromInclusive: Int, toInclusive: Int)
}