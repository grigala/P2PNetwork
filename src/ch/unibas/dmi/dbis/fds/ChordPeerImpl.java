package ch.unibas.dmi.dbis.fds;


public class ChordPeerImpl extends ChordPeerNode {

	/**
	 * The finger table.
	 */
	protected final FingerTable<ChordPeerImpl> finger;
	
	/**
	 * The predecessor in the chord ring.
	 */
	private ChordPeerImpl predecessor;
	
	/**
	 * TRUE if only using successors (simple, linear case).
	 * FALSE if complete implementation.
	 * This setting should probably conditionally enable or disable some behaviour. ;-)
	 */
	private final boolean useSuccessorsOnly;
	private static final boolean DEBUG = true;
	
	/**
	 * Instantiates a new chord peer.
	 * 
	 * @param network the network
	 * @param nodeID the node id
	 */
	public ChordPeerImpl(Network network, String nodeID, boolean useSuccessorsOnly) {
		super(network, nodeID);
		
		this.useSuccessorsOnly = useSuccessorsOnly;
		finger = new FingerTable<ChordPeerImpl>(this,m);
		
		/*
		 * We defer adding ourselves to the network until *after* we have retrieved a node from the existing network.
		 * This means that we will get null if we're the first node out there.
		 */
		ChordPeerImpl n1 = (ChordPeerImpl) network.getRandomPeer();
		network.addPeer(this);
		join(n1);
	}
	
	/**
     * Sets the successor. Shortcut for sett ing the first finger table entry.
     *
	 * @param newSuccessor the new successor
	 */
	private void setSuccessor(ChordPeerImpl newSuccessor) {
        //System.err.println(this + " sets successor to " + newSuccessor);
        finger.get(0).setNode(newSuccessor);
	}
	

	/**
	 * Helper method to get the successor node. (Figure 6, page 6, line 1)
	 * 
	 * @return the successor
	 */
	public final ChordPeerImpl getSuccessor(PeerNode origin) {
		network.logPassedMessage(Message.MessageType.CHORD_GET_SUCCESSOR, origin, this);
		network.logPassedMessage(Message.MessageType.CHORD_GET_SUCCESSOR_RESPONSE, this, origin);
		
		return finger.get(0).getNode();
	}
	
	/**
	 * Gets the predecessor of the current node.
	 * 
	 * @param origin the origin
	 * @return the predecessor
	 */
	protected final ChordPeerImpl getPredecessor(PeerNode origin) {
		network.logPassedMessage(Message.MessageType.CHORD_GET_PREDECESSOR, origin, this);
		network.logPassedMessage(Message.MessageType.CHORD_GET_PREDECESSOR_RESPONSE, this, origin);
		
		return predecessor;
	}
	
	@Override
	public PeerNode getChordPredecessor() {
		return predecessor;
	}

	@Override
	public String dumpChordFingerTable() {
		return finger.toString();
	}

	/**
	 * Sets the predecessor of the current node.
	 * 
	 * @param origin the origin
	 * @param newPredecessor the new predecessor
	 */
	protected final void setPredecessor(PeerNode origin, ChordPeerImpl newPredecessor) {
		network.logPassedMessage(Message.MessageType.CHORD_SET_PREDECESSOR, origin, this);
		
		/* connection handling, let the infrastructure know about the connections we have */ 
		if (predecessor != null) {
			this.removeConnection(predecessor.nodeID);
		}
		this.addConnection(newPredecessor.nodeID);
		predecessor = newPredecessor;
        //System.err.println(this + " sets predecessor to " + newPredecessor);
        network.logPassedMessage(Message.MessageType.CHORD_SET_PREDECESSOR_RESPONSE, this, origin);
	}
	
	/**
	 * Finds the successor node of a given ID. Figure 4, page 5.
	 * 
	 * @param origin the origin
	 * @param id the id
	 * @return the chord peer
	 */
	protected final ChordPeerImpl findSuccessor(PeerNode origin, long id) {
        ChordPeerImpl ret;
        network.logPassedMessage(Message.MessageType.CHORD_FIND_SUCCESSOR, origin, this);

        ret = findPredecessor(origin, id);
        ret = ret.getSuccessor(origin);

        network.logPassedMessage(Message.MessageType.CHORD_FIND_SUCCESSOR_RESPONSE, this, origin);
        return ret;
	}
	
	/**
	 * Find predecessor of a given ID. Figure 4, page 5.
	 * 
	 * @param origin the origin
	 * @param id the id
	 * @return the chord peer
	 */
	protected final ChordPeerImpl findPredecessor(PeerNode origin, long id) {
		ChordPeerImpl ret = null;
		network.logPassedMessage(Message.MessageType.CHORD_FIND_PREDECESSOR, origin, this);

        ret = this;
        while (!network.isHashElementOf(id, ret.n, ret.getSuccessor(origin).n, false, true)) {
            ret = ret.closestPrecedingFinger(origin, id);
        }

        network.logPassedMessage(Message.MessageType.CHORD_FIND_PREDECESSOR_RESPONSE, this, origin);
        return ret;
	}

	/**
	 * Returns the closest finger preceding id. Figure 4, page 5
	 * 
	 * @param origin the origin
	 * @param id the id
	 * @return the chord peer
	 */
	protected final ChordPeerImpl closestPrecedingFinger(PeerNode origin, long id) {
        ChordPeerImpl ret;
        network.logPassedMessage(Message.MessageType.CHORD_CLOSEST_PRECEDING_FINGER, origin, this);

		ret = this;
		for (int i=m-1; i >= 0; --i) {
			ChordPeerImpl node = finger.get(i).getNode();
			if (node == null) continue;
			long hash = node.n;
			if (network.isHashElementOf(hash, n, id, false, false)) {
				ret = finger.get(i).getNode();
				break;
			}
		}
		
		network.logPassedMessage(Message.MessageType.CHORD_CLOSEST_PRECEDING_FINGER_RESPONSE, this, origin);
		return ret;
	}


	/**
	 * Join. This is a slight variation of Figure 7, page 7.
	 * 
	 * @param n1 a random node in the network; 
	 */
	protected void join(ChordPeerImpl n1) {
		setPredecessor(this, this);
		if (n1 != null) {
			setSuccessor(n1.findSuccessor(this, n));
            if (useSuccessorsOnly) {
                stabilize(this);
            }
            // TODO: move keys
		}
		else {
			setSuccessor(this);
		}
	}
	
	/**
	 * Stabilize. Figure 7, page 7.
	 * 
	 * @param origin the origin
	 */
	protected void stabilize(PeerNode origin) {
		network.logPassedMessage(Message.MessageType.CHORD_STABILIZE, origin, this);

        ChordPeerImpl x = getSuccessor(origin).getPredecessor(origin);
        System.out.println("stabilize caller: " + n + "| successor: " + getSuccessor(origin).n + "|successor.predecessor: " + x.n);
        if (getSuccessor(origin).n == this.n || network.isHashElementOf(x.n, this.n, getSuccessor(origin).n,false, false)) {
            //System.out.println("is better");
            setSuccessor(x);
        }
        this.chordNotify(getSuccessor(origin));


        network.logPassedMessage(Message.MessageType.CHORD_STABILIZE_RESPONSE, this, origin);
    }
	
	/**
	 * Chord notify. This should be a very slight variation of the function presented in figure 7, page 7.
	 * The variation should account for the changes in the join() function.
	 * Most importantly, you have to find a way to enforce that not only predecessor,
	 * but also successor pointers are correct immediately after a node join.
	 * (Hint: it's 2 lines of code to achieve this.)
	 * 
	 * @param n1 the node that thinks it might be our successor.
	 */
	public void chordNotify(ChordPeerImpl n1) {
		network.logPassedMessage(Message.MessageType.CHORD_NOTIFY, n1, this);
        System.out.println(this + " calls notify on " + n1);
        if (network.isHashElementOf(this.n, n1.predecessor.n, n1.n, false, false)) {
            System.out.println(this + " is predecessor of " + n1);
            n1.setPredecessor(this, this);
            //setSuccessor(n1);
        } else {
            System.out.println(this + " is NOT predecessor of " + n1);
        }

        /*ChordPeerImpl successor = getSuccessor(this);
        if (successor == this || successor == null || network.isHashElementOf(n1.n, this.n, successor.n, false, false)){
            System.err.println("n1 is successor");
            setSuccessor(n1);
        }*/

        network.logPassedMessage(Message.MessageType.CHORD_NOTIFY_RESPONSE, this, n1);
	}

	@Override
    public void fixFingers(int fromInclusive, int toInclusive) {
        long start;
        ChordPeerImpl tmp;
        stabilize(this);
        for (int i = fromInclusive; i <= toInclusive; i++) {
            tmp = findSuccessor(this, finger.get(i).getStart());
            finger.get(i).setNode(tmp);
        }
    }
	/* 
	 * In Chord, GET requests should only be directed to the node responsible for the data.
	 * Therefore, we retrieve data only locally.
	 * Feel free to modify this method.
	 */
	@Override
	public String getDataItem(PeerNode originOfQuery, String key) {
		//log incoming query message
		network.logPassedMessage(Message.MessageType.GET, originOfQuery, this);
		
		String resData = localData.get(key);

		//log result of query message
		network.logPassedMessage(Message.MessageType.GET_RESPONSE, this, originOfQuery);
		return resData;
	}

	@Override
	public PeerNode lookupNodeForItem(PeerNode originOfQuery, String key) {
		PeerNode node = null;
		//log incoming query message
		network.logPassedMessage(Message.MessageType.LOOKUP, originOfQuery, this);

        long itemId = network.hash(key);
        //trivial case
        if(this.n == itemId){
            node = this;
        }else{
            if(useSuccessorsOnly){
                ChordPeerImpl successor = getSuccessor(this);
                if(network.isHashElementOf(itemId, n, successor.n, false, true)){
                    //is the successor responsible, i.e. is the item higher than my id and lower or equal than the successor?
                    node = getSuccessor(this);
                    if(DEBUG)
                        System.out.println(node + " has the item '" + key + "' with item_id '" + itemId + "'");

                }else{
                    //recursively ask other nodes if their successor is responsible for it
                    if(DEBUG)
                        System.out.println(this + " asks " + successor + "(successor) for item '" + key +  "' with item_id '" + itemId + "'");
                    node = successor.lookupNodeForItem(this, key);
                }
            }else{
                //TODO
                ChordPeerImpl n1 = closestPrecedingFinger(this, itemId);
                if (network.isHashElementOf(n1.n, n, itemId, false, true)){
                    // n1 is closer to the item than me
                    // ask if there is someone closer
                    if(DEBUG)
                        System.out.println(this + " asks " + n1 + " for item '" + key +  "' with item_id '" + itemId + "'");

                    node = n1.lookupNodeForItem(this, key);
                }else{
                    // no one is closer than me
                    // --> my successor is responsible for the item
                    node = this.getSuccessor(this);
                    if(DEBUG)
                        System.out.println(node + " has the item '" + key + "' with item_id '" + itemId + "'");
                }
            }
        }


        //log outgoing message
		network.logPassedMessage(Message.MessageType.LOOKUP_RESPONSE, this, originOfQuery);
		return node;
	}

	/* 
	 * In Chord, SET requests should only be directed to the node responsible for the data.
	 * Therefore, we store data only locally.
	 * Feel free to modify this method.
	 */
	@Override
	public void setDataItem(PeerNode originOfQuery, String key, String value) {
		
		//log save query message
		network.logPassedMessage(Message.MessageType.SET, originOfQuery, this);

		//save data item at destination 
		localData.put(key, value);
		
		//log save query result message
		network.logPassedMessage(Message.MessageType.SET_RESPONSE, originOfQuery, this);
	}
}
