package ch.unibas.dmi.dbis.fds;

public class ChordNetwork extends Network {

    /**
     * Constructor
     *
     * @param numberOfBits bits used for the identifier ring
     */
    public ChordNetwork(int numberOfBits) {
        super(numberOfBits);
    }

    @Override
    final void arrangeOverlayStructure() {
        /* This method does not do anything, as Chord nodes keep the network structure intact automatically. */
    }

    @Override
    public PeerNode createPeer(String id) {
        return new ChordPeerImpl(this, id, false);
    }

    @Override
    public void removePeer(String id) {
        PeerNode peer = getPeer(id);
        if (peer == null) {
            System.err.println("Peer " + id + " not found.");
            return;
        }
        for (String networkID : peer.getConnections()) {
            PeerNode networkPeer = this.getPeer(networkID);
            //remove connections between nodes
            if (networkPeer.hasConnectionTo(id)) {
                networkPeer.removeConnection(id);
            }
            //not sure if both necessary
            if (peer.hasConnectionTo(networkID)) {
                peer.removeConnection(networkID);
            }

            //remove node from hashtable
            nodes.remove(id);
            System.out.println("Removed peer " + peer);
        }

    }
}
