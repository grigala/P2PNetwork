package ch.unibas.dmi.dbis.fds.kotlin

import java.util.*

abstract class PeerNode(var network: Network, val nodeID: String) {
    val localData: Map<String, String> = TreeMap()
    val connections: MutableMap<String, Int> = HashMap() //todo change back to protected

    abstract fun setDataItem(originOfQuery: PeerNode, key: String, value: String)

    abstract fun getDataItem(originOfQuery: PeerNode, key: String): String

    abstract fun lookupNodeForItem(originOfQuery: PeerNode, key: String): PeerNode

    fun hasDataItem(key: String): Boolean {
        return localData.containsKey(key)
    }

    fun addConnection(toId: String) {
        // ignore "connections" to ourselves.
        if (toId == nodeID) return
        synchronized(connections) {
            var count: Int? = connections[toId]
            if (count == null) count = Integer.valueOf(0)
            connections.put(toId, count!!.toInt() + 1)
        }
    }

    fun removeConnection(toId: String) {
        synchronized(connections) {
            val count = connections[toId] ?: // this shouldn't happen, but you never know
                    return
            val newCount = count.toInt() - 1
            if (newCount == 0) {
                connections.remove(toId)
            } else {
                connections.put(toId, newCount)
            }
        }
    }

    fun hasConnectionTo(toID: String): Boolean {
        synchronized(connections) {
            return connections.containsKey(toID)
        }
    }
    //todo uncomment
//    fun getConnections(): Set<String> {
//        synchronized(connections) {
//            return Collections.unmodifiableSet(connections.keys)
//        }
//    }

    override fun toString(): String {
        return "$nodeID  - ${network.hash(nodeID)}"
    }


    open val chordPredecessor: PeerNode?
        get() = null

    open fun dumpChordFingerTable(): String? {
        return null
    }
}
