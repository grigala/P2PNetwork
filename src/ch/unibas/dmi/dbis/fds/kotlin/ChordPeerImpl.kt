package ch.unibas.dmi.dbis.fds.kotlin

class ChordPeerImpl(network: Network, nodeID: String, private val useSuccessorsOnly: Boolean) : ChordPeerNode(network, nodeID) {

    protected val finger: FingerTable<ChordPeerImpl>
    private var predecessor: ChordPeerImpl? = null

    init {
        finger = FingerTable(this, m)

        /*
		 * We defer adding ourselves to the network until *after* we have retrieved a node from the existing network.
		 * This means that we will get null if we're the first node out there.
		 */
        val n1 = network.randomPeer as ChordPeerImpl?
        network.addPeer(this)
        join(n1)
    }

    private fun setSuccessor(newSuccessor: ChordPeerImpl) {
        finger[0].node = newSuccessor
    }

    fun getSuccessor(origin: PeerNode): ChordPeerImpl? {
        network.logPassedMessage(MessageType.CHORD_GET_SUCCESSOR, origin, this)
        network.logPassedMessage(MessageType.CHORD_GET_SUCCESSOR_RESPONSE, this, origin)

        return finger[0].node
    }

    protected fun getPredecessor(origin: PeerNode): ChordPeerImpl? {
        network.logPassedMessage(MessageType.CHORD_GET_PREDECESSOR, origin, this)
        network.logPassedMessage(MessageType.CHORD_GET_PREDECESSOR_RESPONSE, this, origin)

        return predecessor
    }

    override val chordPredecessor: PeerNode?
        get() = predecessor

    override fun dumpChordFingerTable(): String? {
        return finger.toString()
    }

    protected fun setPredecessor(origin: PeerNode, newPredecessor: ChordPeerImpl) {
        network.logPassedMessage(MessageType.CHORD_SET_PREDECESSOR, origin, this)

        /* connection handling, let the infrastructure know about the connections we have */
        if (predecessor != null) {
            this.removeConnection(predecessor!!.nodeID)
        }
        this.addConnection(newPredecessor.nodeID)
        predecessor = newPredecessor

        network.logPassedMessage(MessageType.CHORD_SET_PREDECESSOR_RESPONSE, this, origin)
    }

    protected fun findSuccessor(origin: PeerNode, id: Long): ChordPeerImpl {
        val ret: ChordPeerImpl? = null
        network.logPassedMessage(MessageType.CHORD_FIND_SUCCESSOR, origin, this)

        /* BEGIN IMPLEMENTATION */
        System.err.println("findSuccessor() NOT IMPLEMENTED") //FIXME: your turn!
        /* END IMPLEMENTATION */

        network.logPassedMessage(MessageType.CHORD_FIND_SUCCESSOR_RESPONSE, this, origin)
        return ret!!
    }

    protected fun findPredecessor(origin: PeerNode, id: Long): ChordPeerImpl {
        val ret: ChordPeerImpl? = null
        network.logPassedMessage(MessageType.CHORD_FIND_PREDECESSOR, origin, this)

        /* BEGIN IMPLEMENTATION */
        System.err.println("findPredecessor() NOT IMPLEMENTED")  //FIXME: your turn!
        /* END IMPLEMENTATION */

        network.logPassedMessage(MessageType.CHORD_FIND_PREDECESSOR_RESPONSE, this, origin)
        return ret!!
    }


    protected fun closestPrecedingFinger(origin: PeerNode, id: Long): ChordPeerImpl {
        var ret: ChordPeerImpl? = null
        network.logPassedMessage(MessageType.CHORD_CLOSEST_PRECEDING_FINGER, origin, this)

        ret = this
        for (i in m - 1 downTo 0) {
            val node = finger[i].node ?: continue
            val hash = node.n
            if (network.isHashElementOf(hash, n, id, false, false)) {
                ret = finger[i].node
                break
            }
        }

        network.logPassedMessage(MessageType.CHORD_CLOSEST_PRECEDING_FINGER_RESPONSE, this, origin)
        return ret!!
    }

    protected fun join(n1: ChordPeerImpl?) {
        setPredecessor(this, this)
        if (n1 != null) {
            setSuccessor(n1.findSuccessor(this, n))
            if (useSuccessorsOnly) {
                stabilize(this)
            }
            // TODO: move keys
        } else {
            setSuccessor(this)
        }
    }

    protected fun stabilize(origin: PeerNode) {
        network.logPassedMessage(MessageType.CHORD_STABILIZE, origin, this)

        /* BEGIN IMPLEMENTATION */
        System.err.println("stabilize() NOT IMPLEMENTED") //FIXME: your turn!
        /* END IMPLEMENTATION */

        network.logPassedMessage(MessageType.CHORD_STABILIZE_RESPONSE, this, origin)
    }

    fun chordNotify(n1: ChordPeerImpl) {
        network.logPassedMessage(MessageType.CHORD_NOTIFY, n1, this)

        /* BEGIN IMPLEMENTATION */
        System.err.println("chordNotify() NOT IMPLEMENTED")  //FIXME: your turn!
        /* END IMPLEMENTATION */

        network.logPassedMessage(MessageType.CHORD_NOTIFY_RESPONSE, this, n1)
    }

    override fun fixFingers(fromInclusive: Int, toInclusive: Int) {
        /* BEGIN IMPLEMENTATION */
        System.err.println("fixFingers() NOT IMPLEMENTED")  //FIXME: your turn!
        /* END IMPLEMENTATION */
    }

    /*
     * In Chord, GET requests should only be directed to the node responsible for the data.
     * Therefore, we retrieve data only locally.
     * Feel free to modify this method.
     */
    override fun getDataItem(originOfQuery: PeerNode, key: String): String {
        //log incoming query message
        network.logPassedMessage(MessageType.GET, originOfQuery, this)

        val resData = localData[key]

        //log result of query message
        network.logPassedMessage(MessageType.GET_RESPONSE, this, originOfQuery)
        return resData!!
    }

    override fun lookupNodeForItem(originOfQuery: PeerNode, key: String): PeerNode {
        val node: PeerNode? = null
        //log incoming query message
        network.logPassedMessage(MessageType.LOOKUP, originOfQuery, this)


        /* BEGIN IMPLEMENTATION */
        System.err.println("lookupNodeForItem() NOT IMPLEMENTED")  //FIXME: your turn!
        /* END IMPLEMENTATION */

        //log outgoing message
        network.logPassedMessage(MessageType.LOOKUP_RESPONSE, this, originOfQuery)
        return node!!
    }

    /*
     * In Chord, SET requests should only be directed to the node responsible for the data.
     * Therefore, we store data only locally.
     * Feel free to modify this method.
     */
    override fun setDataItem(originOfQuery: PeerNode, key: String, value: String) {

        //log save query message
        network.logPassedMessage(MessageType.SET, originOfQuery, this)

        //save data item at destination
        //todo localData.put(key, value)

        //log save query result message
        network.logPassedMessage(MessageType.SET_RESPONSE, originOfQuery, this)
    }
}
