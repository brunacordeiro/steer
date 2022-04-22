/*
 * Copyright (C) 2015 Seb
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

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.topology.NetworkGraph;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.List;

/**
 * This Interface defines principal methods of the Controller.
 *
 * @author Sebastiano Milardo
 */
public interface ControllerInterface {

    /**
     * This method adds a new address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newAddr the address
     */
    void addAcceptedAddress(byte netId, NodeAddress destination, NodeAddress newAddr);

    /**
     * This method installs a rule in the node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param rule the rule to be installed
     */
    void addRule(byte netId, NodeAddress destination, FlowTableEntry rule);

    /**
     * This method returns the list of addresses accepted by the node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the list of accepted Addresses
     */
    List<NodeAddress> getAcceptedAddressesList(byte netId, NodeAddress destination);

    /**
     * This method gets the NetworkGraph of the controller
     *
     * @return returns a NetworkGraph object
     */
    NetworkGraph getNetworkGraph();

    /**
     * This method reads the beacon period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the beacon period, -1 if not found
     */
    int getNodeBeaconPeriod(byte netId, NodeAddress destination);

    /**
     * This method reads the report period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the report period, -1 if not found
     */
    int getNodeReportPeriod(byte netId, NodeAddress destination);

    /**
     * This method reads the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the minimum RSSI, -1 if not found
     */
    int getNodeRssiMin(byte netId, NodeAddress destination);

    /**
     * This method reads the maximum time to live for each message sent by a
     * node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the maximum time to live, -1 if not found
     */
    int getNodeTtlMax(byte netId, NodeAddress destination);

    /**
     * This method reads the Update table period of a node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the updateTablePeriod, -1 if not found
     */
    int getNodeUpdateTablePeriod(byte netId, NodeAddress destination);

    /**
     * This method gets the WISE flow table entry of a node at position n
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param index position of the entry in the table
     * @return returns the list of the entries in the WISE Flow Table
     */
    FlowTableEntry getRuleAtPosition(byte netId, NodeAddress destination, int index);

    /**
     * This method gets the WISE flow table of a node
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @return returns the list of the entries in the WISE Flow Table
     */
    List<FlowTableEntry> getRules(byte netId, NodeAddress destination);

    /**
     * Method called to update the graph of Network.
     *
     */
    void graphUpdate();

    /**
     * Method to manage Request about Routing for a NetworkPacket.
     *
     * @param data NetworkPacket will be managed.
     */
    void manageRoutingRequest(NetworkPacket data);

    /**
     * This method removes an address in the list of addresses accepted by the
     * node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newAddr the address
     */
    void removeAcceptedAddress(byte netId, NodeAddress destination, NodeAddress newAddr);

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param index index of the erased row
     */
    void removeRule(byte netId, NodeAddress destination, int index);

    /**
     * This method removes a rule in the node.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param rule the rule to be removed
     */
    void removeRule(byte netId, NodeAddress destination, FlowTableEntry rule);

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     */
    void resetNode(byte netId, NodeAddress destination);

    /**
     * Method to send a function to a Node with following characteristics.
     *
     * @param netId Network Identity the Destination Node belongs.
     * @param destination network address of the destination node
     * @param functionId byte value to identifies functions to send.
     * @param className string value for the function to send.
     */
    void sendFunction(byte netId, NodeAddress destination, byte functionId, String className);

    /**
     * This method sends a SDN_WISE_OPEN_PATH messages to a generic node. This
     * kind of message holds a list of nodes that will create a path inside the
     * network.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param path the list of all the NodeAddresses in the path
     */
    void sendPath(byte netId, NodeAddress destination, List<NodeAddress> path);

    /**
     * This method sets the address of a node. The new address value is passed
     * using two bytes.
     *
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newAddress the new address
     */
    void setNodeAddress(byte netId, NodeAddress destination, NodeAddress newAddress);

    /**
     * This method sets the beacon period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period beacon period in seconds
     */
    void setNodeBeaconPeriod(byte netId, NodeAddress destination, short period);

    /**
     * This method sets the Network ID of a node. The new value is passed using
     * a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newNetId value of the new net ID
     */
    void setNodeNetId(byte netId, NodeAddress destination, byte newNetId);

    /**
     * This method sets the report period of a node. The new value is passed
     * using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period report period in seconds
     */
    void setNodeReportPeriod(byte netId, NodeAddress destination, short period);

    /**
     * This method sets the minimum RSSI in order to consider a node as a
     * neighbor.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newRssi new threshold rssi value
     */
    void setNodeRssiMin(byte netId, NodeAddress destination, byte newRssi);

    /**
     * This method sets the maximum time to live for each message sent by a
     * node. The new value is passed using a byte.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param newTtl time to live in number of hops
     */
    void setNodeTtlMax(byte netId, NodeAddress destination, byte newTtl);

    /**
     * This method sets the update table period of a node. The new value is
     * passed using a short.
     *
     * @param netId network id of the destination node
     * @param destination network address of the destination node
     * @param period update table period in seconds (TODO check)
     */
    void setNodeUpdateTablePeriod(byte netId, NodeAddress destination, short period);

    /**
     * Method called when the network starts. It could be used to configuration
     * rules or network at the beginning of the application.
     */
    void setupNetwork();

}
