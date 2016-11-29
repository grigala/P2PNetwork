package ch.unibas.dmi.dbis.fds.kotlin

import java.util.*

class FingerTable<T : ChordPeerNode>(val owner: T, m: Int) {
    // declared val as public might needed to change it in future.
    inner class Entry(n: Long, m: Int, k: Int) {
        val start: Long
        val end: Long
        var node: T? = null
            set(node) {
                if (this.node != null) {
                    owner.removeConnection(this.node!!.nodeID)
                }
                field = node

                println("FingerTable: finger changed at ${owner.n}: ${toString()}")
                owner.addConnection(node!!.nodeID)
            }

        init {
            this.start = (n + (1 shl k)) % (1 shl m)
            this.end = (n + (1 shl k + 1)) % (1 shl m)
        }

        override fun toString(): String {
            // F*ck JAVA! -> return "[" + start + "," + end + ") : " + this.node
            // Long live Kotlin!
            return "[$start, $end) : ${this.node}"
        }
    }


    private val entries: MutableList<Entry>

    operator fun get(index: Int): Entry {
        return entries[index]
    }

    fun size(): Int {
        return entries.size
    }

    init {
        entries = ArrayList<Entry>(m)
        for (k in 0..m - 1) {
            entries.add(Entry(owner.n, m, k))
        }
    }

    override fun toString(): String {
        val s = StringBuilder()

        for (i in entries.indices) {
            s.append("finger $i: ")
            s.append(entries[i].toString())
            s.append("\n")
        }

        return s.toString()
    }
}
