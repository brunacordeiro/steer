/*
 * Copyright (C) 2015 SDN-WISE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.sdnwiselab.sdnwise.controller;

import com.github.sdnwiselab.sdnwise.adapter.Adapter;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.function.FunctionInterface;
import com.github.sdnwiselab.sdnwise.packet.ConfigAcceptedIdPacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigFunctionPacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigNodePacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigPacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigRulePacket;
import static com.github.sdnwiselab.sdnwise.packet.ConfigRulePacket.SDN_WISE_CNF_GET_RULE_INDEX;
import com.github.sdnwiselab.sdnwise.packet.ConfigSecurityPacket;
import com.github.sdnwiselab.sdnwise.packet.ConfigTimerPacket;
//import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.*;
import com.github.sdnwiselab.sdnwise.packet.OpenPathPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.packet.ResponsePacket;
import com.github.sdnwiselab.sdnwise.topology.NetworkGraph;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jodah.expiringmap.ExpiringMap;

/**
 * This class holds a representation of the sensor network and resolves all the
 * requests coming from the network itself. This abstract class has two main
 * methods. manageRoutingRequest and graphUpdate. The first is called when a
 * request is coming from the network while the latter is called when something
 * in the topology of the network changes.
 * <p>
 * There are two main implementation of this class: ControllerDijkstra and
 * Controller Static.
 * <p>
 * This class also offers methods to send messages and configure the nodes in
 * the network.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public abstract class Controller extends Observable implements Observer, Runnable, ControllerInterface {

    final static int SDN_WISE_RLS_MAX = 16;
    final static int RESPONSE_TIMEOUT = 250;

    private final Adapter lower;
    final Scanner scanner;
    final NetworkGraph networkGraph;
    
    final HashMap<NodeAddress, LinkedList<NodeAddress>> results;
    private boolean isStopped;
    private final ArrayBlockingQueue<NetworkPacket> bQ;

    final Map<String, ConfigPacket> cache = ExpiringMap.builder()
            .expiration(5, TimeUnit.SECONDS)
            .build();

    private final NodeAddress sinkAddress;

    public NodeAddress getSinkAddress() {
        return sinkAddress;
    }

    /**
     * Constructor Method for the Controller Class.
     *
     * @param id ControllerId object.
     * @param lower Lower Adapter object.
     * @param networkGraph NetworkGraph object.
     */
    Controller(Adapter lower, NetworkGraph networkGraph) {
        this.lower = lower;
        bQ = new ArrayBlockingQueue<>(1000);
        this.networkGraph = networkGraph;
        results = new HashMap<>();
        scanner = new Scanner(System.in, "UTF-8");
        isStopped = false;
        sinkAddress = new NodeAddress("0.1");
    }

    public void managePacket(NetworkPacket data) {
        switch (data.getType()) {
            case SDN_WISE_REPORT:
                networkGraph.updateMap(new ReportPacket(data));
            case SDN_WISE_DATA:
            case SDN_WISE_BEACON:
            case SDN_WISE_RESPONSE:
            case SDN_WISE_OPEN_PATH:
                setChanged();
                notifyObservers(data);
                break;
            case SDN_WISE_CONFIG:

                ConfigPacket cp = new ConfigPacket(data);

                if ((cp.getConfigId() == ConfigNodePacket.SDN_WISE_CNF_ID_ADDR) //SDN_WISE_CNF_ID_ADDR = 0
                        || (cp.getConfigId() == ConfigNodePacket.SDN_WISE_CNF_ID_NET_ID) //SDN_WISE_CNF_ID_NET_ID = 1
                        || (cp.getConfigId() == ConfigNodePacket.SDN_WISE_CNF_RESET) //SDN_WISE_CNF_RESET = 15
                        || (cp.getConfigId() == ConfigNodePacket.SDN_WISE_CNF_ID_TTL_MAX) //SDN_WISE_CNF_ID_TTL_MAX = 6
                        || (cp.getConfigId() == ConfigNodePacket.SDN_WISE_CNF_ID_RSSI_MIN)) {               //SDN_WISE_CNF_ID_RSSI_MIN = 7
                    cp = new ConfigNodePacket(data);
                } else if ((cp.getConfigId() == ConfigTimerPacket.SDN_WISE_CNF_ID_CNT_BEACON_MAX) //SDN_WISE_CNF_ID_CNT_BEACON_MAX = 2
                        || (cp.getConfigId() == ConfigTimerPacket.SDN_WISE_CNF_ID_CNT_REPORT_MAX) //SDN_WISE_CNF_ID_CNT_REPORT_MAX = 3
                        || (cp.getConfigId() == ConfigTimerPacket.SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX) //SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX = 4
                        || (cp.getConfigId() == ConfigTimerPacket.SDN_WISE_CNF_ID_CNT_SLEEP_MAX)) {         //SDN_WISE_CNF_ID_CNT_SLEEP_MAX = 5
                    cp = new ConfigTimerPacket(data);
                } else if ((cp.getConfigId() == ConfigAcceptedIdPacket.SDN_WISE_CNF_ADD_ACCEPTED) //SDN_WISE_CNF_ADD_ACCEPTED = 8
                        || (cp.getConfigId() == ConfigAcceptedIdPacket.SDN_WISE_CNF_LIST_ACCEPTED) //SDN_WISE_CNF_LIST_ACCEPTED = 10
                        || (cp.getConfigId() == ConfigAcceptedIdPacket.SDN_WISE_CNF_REMOVE_ACCEPTED)) {     //SDN_WISE_CNF_REMOVE_ACCEPTED = 9
                    cp = new ConfigAcceptedIdPacket(data);
                } else if ((cp.getConfigId() == ConfigRulePacket.SDN_WISE_CNF_ADD_RULE) //SDN_WISE_CNF_ADD_RULE = 11
                        || (cp.getConfigId() == ConfigRulePacket.SDN_WISE_CNF_GET_RULE_INDEX) //SDN_WISE_CNF_GET_RULE_INDEX = 14
                        || (cp.getConfigId() == ConfigRulePacket.SDN_WISE_CNF_REMOVE_RULE) //SDN_WISE_CNF_REMOVE_RULE = 12
                        || (cp.getConfigId() == ConfigRulePacket.SDN_WISE_CNF_REMOVE_RULE_INDEX)) {         //SDN_WISE_CNF_REMOVE_RULE_INDEX = 13
                    cp = new ConfigRulePacket(data);
                } else if ((cp.getConfigId() == ConfigFunctionPacket.SDN_WISE_CNF_ADD_FUNCTION) // SDN_WISE_CNF_ADD_FUNCTION = 16
                        || (cp.getConfigId() == ConfigFunctionPacket.SDN_WISE_CNF_REMOVE_FUNCTION)) {       // SDN_WISE_CNF_REMOVE_FUNCTION = 17
                    cp = new ConfigFunctionPacket(data);
                } else {
                    cp = new ConfigSecurityPacket(data);
                }

                String key;
                if (cp.getPayloadAt(0) == (SDN_WISE_CNF_GET_RULE_INDEX)) {      //SDN_WISE_CNF_GET_RULE_INDEX = 14
                    key = cp.getNetId() + " "
                            + cp.getSrc() + " "
                            + cp.getPayloadAt(0) + " "
                            + cp.getPayloadAt(1) + " "
                            + cp.getPayloadAt(2);
                } else {
                    key = cp.getNetId() + " "
                            + cp.getSrc() + " "
                            + cp.getPayloadAt(0);
                }
                cache.put(key, cp);
                break;
            default:
                if (data.isRequest()) {
                    manageRoutingRequest(data);
                }
                break;
        }
    }

    /**
     * This methods manages updates coming from the lower adapter or the network
     * representation. When a message is received from the lower adapter it is
     * inserted in a ArrayBlockingQueue and then the method managePacket it is
     * called on it. While for updates coming from the network representation
     * the method graphUpdate is invoked.
     *
     * @param o the source of the event.
     * @param arg Object sent by Observable.
     */
    @Override
    public void update(Observable o, Object arg) {              //atualização do adaptador ou da representação da rede
        if (o.equals(lower)) {                                  // adaptador
            try {
                bQ.put(new NetworkPacket((byte[]) arg));        //msg recebida inserida em um ArrayBlockingQueue<NetworkPacket> bQ;

            } catch (InterruptedException ex) {
                log(Level.SEVERE, ex.getMessage());
            }
        } else if (o.equals(networkGraph)) {                     //representação da rede
            graphUpdate();
        }
    }

    /**
     * Starts the working thread that manages incoming requests and it listens
     * to messages coming from the standard input.
     */
    @Override
    public void run() {
        if (lower.open()) {                                 //lower - adaptador
            Thread th = new Thread(new Worker(bQ));
            th.start();
            lower.addObserver(this);
            networkGraph.addObserver(this);
            register();
            setupNetwork();
            while (!isStopped) {
                if (scanner.nextLine().equals("exit -l Controller")) {
                    isStopped = true;
                }
            }
            lower.close();
        }
    }

    /**
     * This method sends a SDN_WISE_OPEN_PATH messages to a generic node. This
     * kind of message holds a list of nodes that will create a path inside the
     * network.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param path the list of all the NodeAddresses in the path.
     */
    @Override
    public final void sendPath(byte netId, NodeAddress destination, List<NodeAddress> path) {
        OpenPathPacket op = new OpenPathPacket(netId, sinkAddress, destination);
        op.setPath(path).setNxhop(sinkAddress);
        sendNetworkPacket(op);
    }

    /**
     * This method sends a generic message to a node. The message is represented
     * by a NetworkPacket.
     *
     * @param packet the packet to be sent.
     */
    public void sendNetworkPacket(NetworkPacket packet) {
        lower.send(packet.toByteArray());
    }

    private ConfigPacket sendQuery(ConfigPacket cp) throws TimeoutException {

        sendNetworkPacket(cp);

        try {
            Thread.sleep(RESPONSE_TIMEOUT);     //RESPONSE_TIMEOUT = 250
        } catch (InterruptedException ex) {
            log(Level.SEVERE, ex.getMessage());
        }

        String key;

        if (cp.getPayloadAt(0) == (SDN_WISE_CNF_GET_RULE_INDEX)) {  //  SDN_WISE_CNF_GET_RULE_INDEX = 14;
            key = cp.getNetId() + " "
                    + cp.getDst() + " "
                    + cp.getPayloadAt(0) + " "
                    + cp.getPayloadAt(1) + " "
                    + cp.getPayloadAt(2);
        } else {
            key = cp.getNetId() + " "
                    + cp.getDst() + " "
                    + cp.getPayloadAt(0);
        }
        if (cache.containsKey(key)) {
            return cache.remove(key);
        } else {
            throw new TimeoutException("No answer from the node");
        }
    }

    /**
     * This method sets the address of a node. The new address value is passed
     * using two bytes.
     *
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddress the new address.
     */
    @Override
    public final void setNodeAddress(byte netId, NodeAddress destination, NodeAddress newAddress) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setNodeAddressValue(newAddress).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the address of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the NodeAddress of a node, null if it does exists.
     */
    public final NodeAddress getNodeAddress(byte netId, NodeAddress destination) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setReadNodeAddressValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return ((ConfigNodePacket) response).getNodeAddress();
    }

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     */
    @Override
    public final void resetNode(byte netId, NodeAddress destination) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setResetValue().setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newNetId value of the new net ID
     */
    @Override
    public final void setNodeNetId(byte netId, NodeAddress destination, byte newNetId) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setNetworkIdValue(newNetId).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the Network ID of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the nedId, -1 if not found.
     */
    public final int getNodeNetId(byte netId, NodeAddress destination) {
        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setReadNetworkIdValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return ((ConfigNodePacket) response).getNetworkIdValue();
    }

    /**
     * This method sets the beacon period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period beacon period in seconds
     */
    @Override
    public final void setNodeBeaconPeriod(byte netId, NodeAddress destination, short period) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setBeaconPeriodValue(period).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the beacon period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the beacon period, -1 if not found
     */
    @Override
    public final int getNodeBeaconPeriod(byte netId, NodeAddress destination) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setReadBeaconPeriodValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;

        }
        return ((ConfigTimerPacket) response).getBeaconPeriodValue();
    }

    /**
     * This method sets the report period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period report period in seconds
     */
    @Override
    public final void setNodeReportPeriod(byte netId, NodeAddress destination, short period) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setReportPeriodValue(period).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the report period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the report period, -1 if not found
     */
    @Override
    public final int getNodeReportPeriod(byte netId, NodeAddress destination) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setReadReportPeriodValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return ((ConfigTimerPacket) response).getReportPeriodValue();
    }

    /**
     * This method sets the update table period of a node. The new value is
     * passed using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period update table period in seconds (TODO check)
     */
    @Override
    public final void setNodeUpdateTablePeriod(byte netId, NodeAddress destination, short period) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setUpdateTablePeriodValue(period).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the Update table period of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the updateTablePeriod, -1 if not found.
     */
    @Override
    public final int getNodeUpdateTablePeriod(byte netId, NodeAddress destination) {

        ConfigTimerPacket cp = new ConfigTimerPacket(netId, sinkAddress, destination);
        cp.setReadUpdateTablePeriodValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return ((ConfigTimerPacket) response).getUpdateTablePeriodValue();
    }

    /**
     * This method sets the maximum time to live for each message sent by a
     * node. The new value is passed using a byte.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newTtl time to live in number of hops.
     */
    @Override
    public final void setNodeTtlMax(byte netId, NodeAddress destination, byte newTtl) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setDefaultTtlMaxValue(newTtl).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the maximum time to live for each message sent by a
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the maximum time to live, -1 if not found.
     */
    @Override
    public final int getNodeTtlMax(byte netId, NodeAddress destination) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setReadDefaultTtlMaxValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return ((ConfigNodePacket) response).getDefaultTtlMaxValue();
    }

    /**
     * This method sets the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newRssi new threshold rssi value.
     */
    @Override
    public final void setNodeRssiMin(byte netId, NodeAddress destination, byte newRssi) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setDefaultRssiMinValue(newRssi).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method reads the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the minimum RSSI, -1 if not found.
     */
    @Override
    public final int getNodeRssiMin(byte netId, NodeAddress destination) {

        ConfigNodePacket cp = new ConfigNodePacket(netId, sinkAddress, destination);
        cp.setReadDefaultRssiMinValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return -1;
        }
        return ((ConfigNodePacket) response).getDefaultRssiMinValue();
    }

    /**
     * This method adds a new address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddr the address.
     */
    @Override
    public final void addAcceptedAddress(byte netId, NodeAddress destination, NodeAddress newAddr) {

        ConfigAcceptedIdPacket cp = new ConfigAcceptedIdPacket(netId, sinkAddress, destination);
        cp.setAddAcceptedAddressValue(newAddr).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method removes an address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param newAddr the address.
     */
    @Override
    public final void removeAcceptedAddress(byte netId, NodeAddress destination, NodeAddress newAddr) {

        ConfigAcceptedIdPacket cp = new ConfigAcceptedIdPacket(netId, sinkAddress, destination);
        cp.setRemoveAcceptedAddressValue(newAddr).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method returns the list of addresses accepted by the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the list of accepted Addresses.
     */
    @Override
    public final List<NodeAddress> getAcceptedAddressesList(byte netId, NodeAddress destination) {

        ConfigAcceptedIdPacket cp = new ConfigAcceptedIdPacket(netId, sinkAddress, destination);
        cp.setReadAcceptedAddressesValue().setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return new ConfigAcceptedIdPacket(response).getAcceptedAddressesValues();
    }

    /**
     * This method installs a rule in the node
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param rule the rule to be installed.
     */
    @Override
    public final void addRule(byte netId, NodeAddress destination, FlowTableEntry rule) {
        /*
         ConfigPacket cp = new ConfigPacket();
         cp.setAddRuleValue(rule)
         .setNetId(netId)
         .setDst(destination)
         .setSrc(sinkAddress)
         .setNxhop(sinkAddress);
         sendNetworkPacket(cp);
         */

        ResponsePacket rp = new ResponsePacket(netId, sinkAddress, destination);
        rp.setRule(rule).setNxhop(sinkAddress);
        sendNetworkPacket(rp);
    }

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param index index of the erased row.
     */
    @Override
    public final void removeRule(byte netId, NodeAddress destination, int index) {

        ConfigRulePacket cp = new ConfigRulePacket(netId, sinkAddress, destination);
        cp.setRemoveRuleAtPositionValue(index).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param rule the rule to be removed.
     */
    @Override
    public final void removeRule(byte netId, NodeAddress destination, FlowTableEntry rule) {

        ConfigRulePacket cp = new ConfigRulePacket(netId, sinkAddress, destination);
        cp.setRemoveRuleValue(rule).setNxhop(sinkAddress);
        sendNetworkPacket(cp);
    }

    /**
     * This method gets the WISE flow table of a node.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @return returns the list of the entries in the WISE Flow Table.
     */
    @Override
    public final List<FlowTableEntry> getRules(byte netId, NodeAddress destination) {

        List<FlowTableEntry> list = new ArrayList<>(SDN_WISE_RLS_MAX);      //SDN_WISE_RLS_MAX = 16
        for (int i = 0; i < SDN_WISE_RLS_MAX; i++) {
            list.add(i, getRuleAtPosition(netId, destination, i));
        }
        return list;
    }

    /**
     * This method gets the WISE flow table entry of a node at position n.
     *
     * @param netId network id of the destination node.
     * @param destination network address of the destination node.
     * @param index position of the entry in the table.
     * @return returns the list of the entries in the WISE Flow Table.
     */
    @Override
    public final FlowTableEntry getRuleAtPosition(byte netId, NodeAddress destination, int index) {

        ConfigRulePacket cp = new ConfigRulePacket(netId, sinkAddress, destination);
        cp.setReadRuleAtPositionValue(index).setNxhop(sinkAddress);
        ConfigPacket response;

        try {
            response = sendQuery(cp);
        } catch (TimeoutException ex) {
            log(Level.SEVERE, ex.getMessage());
            return null;
        }
        return ((ConfigRulePacket) response).getRule();
    }

    /**
     * This method is used to register the Controller with the FlowVisor.
     */
    //TODO we need to implement same sort of security check/auth.
    private void register() {
    }

    private List<NetworkPacket> createPackets(
            byte netId,
            NodeAddress src,
            NodeAddress dest,
            NodeAddress nextHop,
            byte id,
            byte[] buf) {

        ConfigFunctionPacket np = new ConfigFunctionPacket(netId, src, dest);
        LinkedList<NetworkPacket> ll = new LinkedList<>();

        np.setNxhop(nextHop);

        int packetNumber = buf.length / 101;
        int remaining = buf.length % 101;
        int totalPackets = packetNumber + (remaining > 0 ? 1 : 0);
        int pointer = 0;
        int i = 0;

        if (packetNumber < 256) {
            if (packetNumber > 0) {
                for (i = 0; i < packetNumber; i++) {
                    byte[] payload = new byte[103];
                    payload[0] = (byte) (i + 1);
                    payload[1] = (byte) totalPackets;
                    System.arraycopy(buf, pointer, payload, 2, 101);
                    pointer += 101;
                    np.setAddFunctionAtPositionValue(id, payload);
                    try {
                        ll.add(np.clone());
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (remaining > 0) {
                byte[] payload = new byte[remaining + 2];
                payload[0] = (byte) (i + 1);
                payload[1] = (byte) totalPackets;
                System.arraycopy(buf, pointer, payload, 2, remaining);
                np.setAddFunctionAtPositionValue(id, payload);
                try {
                    ll.add(np.clone());
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return ll;
    }

    /**
     * Logs messages depending on the verbosity level.
     *
     * @param level a standard logging level.
     * @param msg the string message to be logged.
     */
    public void log(Level level, String msg) {
        Logger.getLogger(this.getClass().getName()).log(level, "[ADA]: {0}", msg);
    }

    @Override
    public void sendFunction(
            byte netId,
            NodeAddress dest,
            byte id,
            String className) {

        try {
            URL main = FunctionInterface.class.getResource(className);
            File path = new File(main.getPath());
            byte[] buf = Files.readAllBytes(path.toPath());

            List<NetworkPacket> ll = createPackets(netId, sinkAddress, dest, sinkAddress, id, buf);

            Iterator<NetworkPacket> llIterator = ll.iterator();

            if (llIterator.hasNext()) {
                this.sendNetworkPacket(llIterator.next());
                Thread.sleep(200);

                while (llIterator.hasNext()) {
                    this.sendNetworkPacket(llIterator.next());
                }
            }
        } catch (IOException ex) {
            log(Level.SEVERE, ex.getMessage());
        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method gets the NetworkGraph of the controller.
     *
     * @return returns a NetworkGraph object.
     */
    @Override
    public NetworkGraph getNetworkGraph() {
        return networkGraph;
    }

    private class Worker implements Runnable {

        private final ArrayBlockingQueue<NetworkPacket> bQ;
        boolean isStopped = false;

        Worker(ArrayBlockingQueue<NetworkPacket> bQ) {
            this.bQ = bQ;
        }

        @Override
        public void run() {
            while (!isStopped) {
                try {
                    managePacket(bQ.take());
                } catch (InterruptedException ex) {
                    isStopped = true;
                }
            }
        }
    }
}
