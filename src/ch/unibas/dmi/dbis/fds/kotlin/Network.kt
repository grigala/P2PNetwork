package ch.unibas.dmi.dbis.fds.kotlin


import java.awt.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Copyright 2016 Giorgi Grigalashvili
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
abstract class Network protected constructor(val numberOfBits: Int) {

    private inner class GuiPanel : javax.swing.JPanel() {

        override fun getPreferredSize(): Dimension {
            return Dimension(500, 550)
        }

        override fun paint(g: Graphics?) {
            val g2d = g as Graphics2D?

            // no nodes nothing to draw!
            if (nodes.size == 0) {
                return
            }

            val nodeStroke = BasicStroke(2.0f)

            val connectionStroke = BasicStroke(1f,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f,
                    floatArrayOf(2f, 6f), 0f)

            val messageStroke = BasicStroke(1f,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f,
                    floatArrayOf(10f, 20f), 0f)

            val duplicateHashCheck = ArrayList<Long>()

            g2d!!.color = Color.YELLOW
            g2d.drawOval(20, 20, 400, 400)

            var numberOfConnections: Long = 0

            // Draw client node
            g2d.color = Color.RED
            g2d.stroke = nodeStroke
            g2d.drawRect(5, 5, 10, 10)

            for (p in nodes.values) {

                // Duplicate check
                val nodeHash = hash(p.nodeID)
                if (!duplicateHashCheck.contains(nodeHash))
                    duplicateHashCheck.add(nodeHash)
                else {
                    System.err.println("Node hash duplicate for ${p.nodeID}!")
                }

                // Draw peer nodes
                val alpha = getAngleForNode(p.nodeID)
                g2d.color = Color.BLUE
                g2d.stroke = nodeStroke
                g2d.drawRect((220 - 5 + 200 * Math.sin(alpha)).toInt(),
                        (220 - 5 + 200 * Math.cos(alpha)).toInt(), 10, 10)
                g2d.drawString(nodeHash.toString(),
                        (220 - 5 + 200 * Math.sin(alpha)).toInt() + (2 + 14 * Math.sin(alpha)).toInt(),
                        (230 - 5 + 200 * Math.cos(alpha)).toInt() + (18 * Math.cos(alpha)).toInt())

                // Draw connection lines
                for (toID in p.connections) {
                    val alpha2 = getAngleForNode(toID.toString())
                    g2d.color = Color.GRAY
                    g2d.stroke = connectionStroke
                    numberOfConnections++
                    g2d.drawLine((220 + 200 * Math.sin(alpha)).toInt(),
                            (220 + 200 * Math.cos(alpha)).toInt(),
                            (220 + 200 * Math.sin(alpha2)).toInt(),
                            (220 + 200 * Math.cos(alpha2)).toInt())
                }

            }

            val numberOfMessages = passedMessages.size

            // Draw message lines
            for (m in passedMessages) {
                val alpha1: Double
                val alpha2: Double
                var x1 = 10
                var y1 = 10
                var x2 = 10
                var y2 = 10
                if (m.sourceNodeId != null) {
                    alpha1 = getAngleForNode(m.sourceNodeId)
                    x1 = (220 + 200 * Math.sin(alpha1)).toInt()
                    y1 = (220 + 200 * Math.cos(alpha1)).toInt()
                }
                if (m.destinationNodeId != null) {
                    alpha2 = getAngleForNode(m.destinationNodeId)
                    x2 = (220 + 200 * Math.sin(alpha2)).toInt()
                    y2 = (220 + 200 * Math.cos(alpha2)).toInt()
                }
                when (m.msgType) {
                    MessageType.GET -> g2d.color = Color.GREEN
                    MessageType.GET_RESPONSE -> g2d.color = Color.GREEN
                    MessageType.SET -> g2d.color = Color.ORANGE
                    MessageType.SET_RESPONSE -> g2d.color = Color.ORANGE
                }
                g2d.stroke = messageStroke
                g2d.drawLine(x1, y1, x2, y2)
            }

            val numberOfLookupQueries = getNumberOfMessages(MessageType.LOOKUP)
            val numberOfGetQueries = getNumberOfMessages(MessageType.GET)
            val numberOfSaveQueries = getNumberOfMessages(MessageType.SET)

            g2d.color = Color.BLACK
            g2d.drawString("Number of peers: " + nodes.size, 20, 450)

            g2d.drawString("Number of connections: " + numberOfConnections
                    + " per Peer: " + numberOfConnections / nodes.size, 20,
                    470)

            g2d.drawString("Number of lookup/get/save queries: " + numberOfLookupQueries + "/" + numberOfGetQueries
                    + "/" + numberOfSaveQueries, 20, 490)

            g2d.drawString("Number of messages: " + numberOfMessages, 20, 510)
        }

        private fun getAngleForNode(nodeId: String): Double {
            return hash(nodeId).toDouble() / Math.pow(2.0, numberOfBits.toDouble()) * 2.0 * Math.PI
        }

    }

    private val panel = GuiPanel()

    protected val nodes: MutableMap<String, PeerNode> = ConcurrentHashMap()

    private val passedMessages = ArrayList<Message>()

    init {
        if (numberOfBits < 2 || numberOfBits > 56) {
            throw RuntimeException("Number of bits: $numberOfBits not supported!")
        }
    }

    fun addPeer(node: PeerNode) {
        nodes.put(node.nodeID, node)
    }

    fun getPeer(nodeId: String): PeerNode? {
        return nodes[nodeId]
    }

    val randomPeer: PeerNode?
        get() {
            if (nodes.size == 0) return null
            val i = (Math.random() * nodes.size).toInt()
            return nodes.values.toTypedArray()[i]
        }

    abstract fun arrangeOverlayStructure()

    fun clearLogs() {
        passedMessages.clear()
    }

    fun logPassedMessage(msgType: MessageType, fromPeer: PeerNode?, toPeer: PeerNode?) {
        var fromID: String? = null
        var toID: String? = null
        if (fromPeer != null)
            fromID = fromPeer.nodeID
        if (toPeer != null)
            toID = toPeer.nodeID
        // ignore local calls
        if (fromPeer != null && fromPeer == toPeer) {
            // skip
        } else {
            val msg = Message(msgType, fromID!!, toID!!)
            passedMessages.add(msg)
        }
    }

    private fun getNumberOfMessages(msgType: MessageType): Int {
        var ret = 0
        for (m in passedMessages) {
            if (m.msgType == msgType)
                ret++
        }
        return ret
    }

    val messages: List<Message>
        get() = ArrayList(passedMessages)

    fun hash(value: String): Long {
        try {
            val basis = Math.pow(2.0, this.numberOfBits.toDouble()).toInt()
            val md = MessageDigest.getInstance("SHA")
            val digest = md.digest(value.toByteArray())
            var v: Long = 0
            for (i in 0..6) {
                v = v + (digest[i].toInt() and 0xff) * Math.pow(256.0, i.toDouble()).toInt()
            }
            v = v % basis
            return v
        } catch (e: NoSuchAlgorithmException) {
            System.err.println("Hash not supported by your JVM!")
            return -1
        }

    }

    private fun isHashInRingSector(hashID: Long, startOfSectorHashID: Long, endOfSectorHashID: Long): Boolean {

        // Check if 0 is within the given sector
        var crossingZero = false
        if (startOfSectorHashID > endOfSectorHashID)
            crossingZero = true

        // Comparison to check whether hashID is in the sector
        if (!crossingZero) {
            // 0 is not within the sector (the easy case :-) )
            // in this case if hashID is bigger than end or smaller than start
            // => we know hashID is outside
            if (hashID > endOfSectorHashID || hashID < startOfSectorHashID)
                return false
            else
                return true
        } else {
            // 0 is within the sector (the complex case :-( )
            // in this case if hashID is greater/equal than start or
            // smaller/equal than end => we know hashID is inside
            if (hashID >= startOfSectorHashID || hashID <= endOfSectorHashID)
                return true
            else
                return false
        }

    }

    fun isHashElementOf(hash: Long, start: Long, end: Long, startInclusive: Boolean, endInclusive: Boolean): Boolean {
        var start = start
        var end = end
        //System.out.print(hash+" e " + (startInclusive ? "[":"(") + start + "," + end + (endInclusive ? "]":")") +" : ");
        if (!startInclusive && !endInclusive) {
            // special case: open interval between (i, i+1) is always empty, but the following
            // calculations would screw up.
            // the same is true for (2^m-1, 0)
            if (start == end - 1) return false
            if (start == ((1 shl numberOfBits) - 1).toLong() && end.equals(0)) return false
        }
        if (!startInclusive) {
            start++
            if (start == (1 shl numberOfBits).toLong()) start = 0
        }
        if (!endInclusive) {
            end--
            if (end.equals(-1)) end += (1 shl numberOfBits).toLong()
        }
        val ret = isHashInRingSector(hash, start, end)
        return ret
    }

    abstract fun createPeer(id: String): PeerNode
}

