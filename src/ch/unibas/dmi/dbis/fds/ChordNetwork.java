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
}
