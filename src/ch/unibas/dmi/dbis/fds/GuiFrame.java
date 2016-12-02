package ch.unibas.dmi.dbis.fds;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;


/**
 * User interface class of the exercise and contains also the main method.
 *
 * @author Gert Brettlecker
 * @author Christoph Langguth
 */
public class GuiFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;


    /**
     * Creates a network. "Poor man's factory method" - replace the method code to switch to another implementation.
     *
     * @return the network
     */
    private Network createNetwork() {
        return Configuration.createNetwork();
    }

    /**
     * Creates a peer and registers it in the GUI model.
     *
     * @param id the peer id
     * @return the peer node
     */
    private PeerNode createPeer(String id) {
        PeerNode peer = network.createPeer(id);
        peersModel.addElement(peer);
        return peer;
    }


    /**
     * Network used for this user interface session.
     */
    private Network network;

	/* GUI elements */
    private final DefaultComboBoxModel peersModel = new DefaultComboBoxModel();
    private final JComboBox boxPeers = new JComboBox(peersModel);
    private final JButton buttonSetData = new JButton("Set");
    private final JButton buttonGetData = new JButton("Get");
    private final JButton buttonClearLog = new JButton("Clear log");
    private final JTextField textKey = new JTextField("Test", 20);
    private final JTextField textValue = new JTextField(20);
    private final JTextArea textLog = new JTextArea(10, 25);

    /* CHORD only */
    private final JRadioButton buttonFingersPeriodic = new JRadioButton("periodic random");
    private final JRadioButton buttonFingersManual = new JRadioButton("manual");
    private final JButton buttonFingersUpdate = new JButton("Update");
    private final JTextField textFingersInterval = new JTextField("" + Configuration.DEFAULT_FINGER_UPDATE_INTERVAL, 5);
    private final JTextField textFingersStart = new JTextField("0", 3);
    private final JTextField textFingersEnd = new JTextField(3);
    private Timer fingerTableUpdateTimer;

    /**
     * Constructor for user interface
     */
    public GuiFrame() {
        super();
        network = createNetwork();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBackground(Color.lightGray);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(network.getPanel(), BorderLayout.NORTH);
        
        
        /* GUI stuff */
        JPanel uiPanel = new JPanel(new BorderLayout());
        textLog.setEditable(false);
        uiPanel.add(new JScrollPane(textLog), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        BoxLayout buttonsPanelLayout = new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS);
        buttonsPanel.setLayout(buttonsPanelLayout);

        JPanel panel = new JPanel(new FlowLayout());

        boxPeers.addActionListener(this);
        peersModel.addElement("Network / random peer");
        panel.add(boxPeers);
        buttonGetData.addActionListener(this);
        panel.add(buttonGetData);
        buttonSetData.addActionListener(this);
        panel.add(buttonSetData);
        buttonClearLog.addActionListener(this);
        panel.add(buttonClearLog);

        buttonsPanel.add(panel);

        panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("key:"));
        panel.add(textKey);
        panel.add(new JLabel("value:"));
        panel.add(textValue);
        buttonsPanel.add(panel);

        panel = createFingerPanel();
        if (panel != null) buttonsPanel.add(panel);

        uiPanel.add(buttonsPanel, BorderLayout.NORTH);
        getContentPane().add(uiPanel, BorderLayout.CENTER);
        pack();
    }

    private JPanel createFingerPanel() {
        if (!(network instanceof ChordNetwork)) return null;
        JPanel outer = new JPanel();
        BoxLayout horizontal = new BoxLayout(outer, BoxLayout.X_AXIS);
        outer.setLayout(horizontal);

        buttonFingersManual.addActionListener(this);
        buttonFingersPeriodic.addActionListener(this);
        buttonFingersUpdate.addActionListener(this);

        ButtonGroup choice = new ButtonGroup();
        choice.add(buttonFingersPeriodic);
        choice.add(buttonFingersManual);
        buttonFingersManual.setSelected(true);

        JPanel inner = new JPanel();
        BoxLayout vertical = new BoxLayout(inner, BoxLayout.Y_AXIS);
        inner.setLayout(vertical);
        inner.add(buttonFingersPeriodic);

        JPanel line = new JPanel(new FlowLayout());
        line.add(new JLabel("every"));
        line.add(textFingersInterval);
        line.add(new JLabel("ms"));
        inner.add(line);
        outer.add(inner);

        inner = new JPanel();
        vertical = new BoxLayout(inner, BoxLayout.Y_AXIS);
        inner.setLayout(vertical);
        inner.add(buttonFingersManual);

        line = new JPanel(new FlowLayout());
        line.add(new JLabel("from index"));
        line.add(textFingersStart);
        line.add(new JLabel("to index"));
        textFingersEnd.setText("" + (network.getNumberOfBits() - 1));
        line.add(textFingersEnd);
        line.add(buttonFingersUpdate);
        inner.add(line);
        outer.add(inner);

        outer.add(inner);

        fingerTableUpdateTimer = new Timer(Configuration.DEFAULT_FINGER_UPDATE_INTERVAL, new FingerTableUpdateAction(network));
        fingerTableUpdateTimer.setInitialDelay(Configuration.DEFAULT_FINGER_UPDATE_INTERVAL);
        fingerTableUpdateTimer.setRepeats(true);

        return outer;
    }

    /**
     * Main method of application
     *
     * @param args no args needed
     */
    public static void main(String[] args) {
        try {

            GuiFrame frame = new GuiFrame();
            Network network = frame.getNetwork();

            frame.setTitle("Peer2Peer Exercise");
            frame.setVisible(true);


            for (int i = 0; i < Configuration.INITIAL_NODES; i++) {
                frame.createPeer("Node_" + i);
            }


            //Create the overlay structure of the network
            network.arrangeOverlayStructure();

            //save an entry at a random node
            String key = "Test";
            PeerNode node = network.getRandomPeer().lookupNodeForItem(null, key);

            node.setDataItem(null, key, "Value");

            // re-reretrieve the entry starting from a random node
            node = network.getRandomPeer().lookupNodeForItem(null, key);
            node.getDataItem(null, key);

            //draw the network
            frame.updateLog(null);
            frame.repaint();

        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }

    /**
     * Get the network object from the user interface
     *
     * @return network object instance
     */
    public Network getNetwork() {
        return network;
    }

    public void actionPerformed(ActionEvent e) {
        PeerNode node = null;
        Object o = boxPeers.getSelectedItem();
        if (o instanceof PeerNode) {
            node = (PeerNode) o;
        }
        if (e.getSource().equals(buttonGetData)) {
            String key = textKey.getText();
            getData(node, key);
        } else if (e.getSource().equals(buttonSetData)) {
            String key = textKey.getText();
            String value = textValue.getText();
            setData(node, key, value);
        } else if (e.getSource().equals(boxPeers)) {
            // the logic is already being done "outside",
            // we just need a trigger for performing it

            // just enable/disable the clear log button as required
            buttonClearLog.setEnabled(!(boxPeers.getSelectedItem() instanceof PeerNode));
        } else if (e.getSource().equals(buttonClearLog)) {
            network.clearLogs();
            this.createPeer("TEXT");
        } else if (e.getSource().equals(buttonFingersUpdate)) {
            updateFingers(node);
        }
        buttonFingersUpdate.setEnabled(o instanceof PeerNode && buttonFingersManual.isSelected());
        updateTimer();
        updateLog(node);
        repaint();
    }

    private void updateTimer() {
        if (fingerTableUpdateTimer == null) return;
        boolean shouldRun = buttonFingersPeriodic.isSelected();
        int delay = Configuration.DEFAULT_FINGER_UPDATE_INTERVAL;
        try {
            delay = Integer.parseInt(textFingersInterval.getText());
        } catch (NumberFormatException e) {
            System.err.println("Invalid interval value \"" + textFingersInterval.getText() + "\", using " + Configuration.DEFAULT_FINGER_UPDATE_INTERVAL);
        }
        if (delay != fingerTableUpdateTimer.getDelay()) {
            fingerTableUpdateTimer.setDelay(delay);
            fingerTableUpdateTimer.setInitialDelay(delay);
        }
        if (shouldRun && !fingerTableUpdateTimer.isRunning()) {
            fingerTableUpdateTimer.start();
        } else if (!shouldRun && fingerTableUpdateTimer.isRunning()) {
            fingerTableUpdateTimer.stop();
        }
    }

    private void updateFingers(PeerNode node) {
        try {
            int from = Integer.parseInt(textFingersStart.getText());
            int to = Integer.parseInt(textFingersEnd.getText());

            FingerTableUpdateAction.perform(node, from, to);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
    }

    private void updateLog(PeerNode node) {
        StringBuilder s = new StringBuilder();
        if (node != null) {
            // node view
            s.append("NODE ");
            s.append(node.toString());
            PeerNode p = node.getChordPredecessor();
            if (p != null) {
                s.append("\npredecessor: " + p + "\n");
            }
            String fingers = node.dumpChordFingerTable();
            if (fingers != null) {
                s.append(fingers);
            }
            s.append("DATA\n");
            for (Map.Entry<String, String> entry : node.getLocalData().entrySet()) {
                s.append(entry.getKey());
                s.append(": ");
                s.append(entry.getValue());
                s.append("\n");
            }
        } else {
            s.append("NETWORK\n");
            for (Message message : network.getMessages()) {
                s.append(message);
                s.append("\n");
            }
        }
        textLog.setText(s.toString());
    }

    private void getData(PeerNode node, String key) {
        if (node == null) node = network.getRandomPeer();

//		node = node.lookupNodeForItem(node, key);
//		String value = node.getDataItem(node, key);
        node = node.lookupNodeForItem(null, key);
        String value = node.getDataItem(null, key);
        if (value == null) {
            value = "Key \"" + key + "\" not found.";
        }
        textValue.setText(value);
    }

    private void setData(PeerNode node, String key, String value) {
        if (node == null) node = network.getRandomPeer();

        node = node.lookupNodeForItem(node, key);

        node.setDataItem(node, key, value);
    }
}

