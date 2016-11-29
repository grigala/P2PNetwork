package ch.unibas.dmi.dbis.fds.kotlin

object Configuration {
    private val DEBUG_MODE = true

    val INITIAL_NODES: Int
    val NETWORK_BITS: Int
    val DEFAULT_FINGER_UPDATE_INTERVAL = 2000

    init {
        if (DEBUG_MODE) {
            INITIAL_NODES = 5
            NETWORK_BITS = 3
        } else {
            INITIAL_NODES = 20
            NETWORK_BITS = 24
        }
    }

    fun createNetwork(): Network {
        //return new FullyConnectedNetwork(NETWORK_BITS);
        return ChordNetwork(NETWORK_BITS)
    }
}
