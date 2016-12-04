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
        ChordPeerImpl peer = (ChordPeerImpl) getPeer(id);

        if(peer==null){
            System.err.println("Peer " + id + " not found.");
            return;
        }
        //make peer.successor handle data of peer
        peer.quitNetwork();


        //remove peer from gui
        nodes.remove(id);
        System.out.println("Removed peer " + peer);


    }
}
