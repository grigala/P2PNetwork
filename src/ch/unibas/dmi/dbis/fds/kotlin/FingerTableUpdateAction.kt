package ch.unibas.dmi.dbis.fds.kotlin

import java.awt.event.ActionEvent
import java.util.*
import javax.swing.AbstractAction

class FingerTableUpdateAction(private val network: Network) : AbstractAction() {
    private val random = Random()

    override fun actionPerformed(arg0: ActionEvent) {
        val node = network.randomPeer
        val index = random.nextInt(network.numberOfBits)
        if (node != null) {
            perform(node, index, index)
        }
    }

    companion object {
        fun perform(node: PeerNode, from: Int, to: Int) {
            var from = from
            var to = to
            val chord = node as ChordPeerNode
            if (from <= 0)
                from = 0
            if (to >= chord.m)
                to = chord.m - 1
            chord.fixFingers(from, to)
        }
    }
}
