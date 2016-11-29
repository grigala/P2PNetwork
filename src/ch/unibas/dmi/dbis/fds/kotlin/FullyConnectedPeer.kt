package ch.unibas.dmi.dbis.fds.kotlin

class FullyConnectedPeer(network: Network, nodeID: String) : PeerNode(network, nodeID) {

    init {
        network.addPeer(this)
    }

    override fun setDataItem(originOfQuery: PeerNode, key: String, data: String) {

        //log save query message
        network.logPassedMessage(MessageType.SET, originOfQuery, this)

        //save data item at destination
        //localData.put(key, data) todo

        //log save query result message
        network.logPassedMessage(MessageType.SET_RESPONSE, originOfQuery, this)
    }

    override fun getDataItem(originOfQuery: PeerNode, key: String): String {
        var resData: String? = null

        //log incoming query message
        network.logPassedMessage(MessageType.GET, originOfQuery, this)

        //Check if data is locally available
        resData = localData[key]

        //not local and origin of query is client then pass query message to all connections ("broadcast")
        if (resData == null && originOfQuery == null) {

            //Do broadcast to all
            for (nodeId in connections.keys) {
                val p = network.getPeer(nodeId)
                val broadcastResult = p!!.getDataItem(this, key)
                //check if result available
                if (broadcastResult != null) {
                    //store result of query message
                    resData = broadcastResult
                    // return on first success
                    break
                }
            }
        }

        //log result of query message
        network.logPassedMessage(MessageType.GET_RESPONSE, this, originOfQuery)

        return resData!!
    }


    override fun lookupNodeForItem(originOfQuery: PeerNode, key: String): PeerNode {
        // in this kind of network, there is no notion of a node being "responsible" for a particular item,
        // so we always return the current node. Note that this implies that the getDataItem() method
        // has to "recursively" look up data.

        //log incoming query message
        network.logPassedMessage(MessageType.LOOKUP, originOfQuery, this)
        //log outgoing message
        network.logPassedMessage(MessageType.LOOKUP_RESPONSE, this, originOfQuery)
        return this
    }
}
