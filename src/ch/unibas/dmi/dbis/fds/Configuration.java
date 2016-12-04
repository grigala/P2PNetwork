package ch.unibas.dmi.dbis.fds;

public class Configuration {
	/**
	 * true:  "DEBUG" mode
	 * false: "PRODUCTION" mode
	 */
	private static final boolean DEBUG_MODE = true;


	public static final int INITIAL_NODES;
	public static final int NETWORK_BITS;
	public static final int DEFAULT_FINGER_UPDATE_INTERVAL = 1000;

	static {
		if (DEBUG_MODE) {
			INITIAL_NODES = 5;
			NETWORK_BITS = 4;
		} else {
			INITIAL_NODES = 20;
			NETWORK_BITS = 24;
		}
	}

	public static Network createNetwork() {
		//return new FullyConnectedNetwork(NETWORK_BITS);
		return new ChordNetwork(NETWORK_BITS);
	}
}
