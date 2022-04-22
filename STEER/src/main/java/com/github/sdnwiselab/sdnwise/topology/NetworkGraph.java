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
package com.github.sdnwiselab.sdnwise.topology;

import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

/**
 * This class holds a org.graphstream.graph.Graph object which represent the
 * topology of the wireless sensor network. The method updateMap is invoked when
 * a message with topology updates is sent to the controller.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class NetworkGraph extends Observable {

    final Graph graph;
    private long lastModification;
    private final int timeout;
    final int rssiResolution;
    private long lastCheck;

    /**
     * This constructor returns the NetworkGraph object. It requires a time to
     * live for each node in the network and a value representing the RSSI
     * resolution in order to consider a change of the RSSI value a change in
     * the network.
     *
     * @param timeout the time to live for a node in seconds
     * @param rssiResolution the RSSI resolution
     */
    public NetworkGraph(int timeout, int rssiResolution) {
        this.graph = new MultiGraph("SDN-WISE Network");
        this.lastModification = Long.MIN_VALUE;
        this.rssiResolution = rssiResolution;
        this.timeout = timeout;
        this.lastCheck = System.currentTimeMillis();
        graph.setAutoCreate(true);
        graph.setStrict(false);
    }

    /**
     * Returns the last time instant when the NetworkGraph was updated.
     *
     * @return a long representing the last time instant when the NetworkGraph
     * was updated
     */
    public final synchronized long getLastModification() {
        return lastModification;
    }

    /**
     * This method gets the Graph contained in the NetworkGraph
     *
     * @return returns a Graph object
     */
    public Graph getGraph() {
        return graph;
    }

    final boolean checkConsistency(long now) {
        boolean modified = false;
        if (now - lastCheck > (timeout * 1000L)) {
            lastCheck = now;
            for (Node n : graph) {
                if (n.getAttribute("lastSeen", Long.class) != null) {
                    if (!isAlive(timeout, (long) n.getNumber("lastSeen"), now)) {
                        removeNode(n);
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }

    /**
     * This method is invoked when a message with topology updates is received
     * by the controller. It updates the network topology according to the
     * message and checks if all the nodes in the network are still alive.
     *
     * @param packet the NetworkPacket received
     */
    public final synchronized void updateMap(ReportPacket packet) {

        long now = System.currentTimeMillis();
        boolean modified = checkConsistency(now);

        int netId = packet.getNetId();
        int batt = packet.getBatt();
        String nodeId = packet.getSrc().toString();
        String fullNodeId = netId + "." + nodeId;
        NodeAddress addr = packet.getSrc();

        Node node = getNode(fullNodeId);

        if (node == null) {
            node = addNode(fullNodeId);
            setupNode(node, batt, now, netId, addr);

            for (int i = 0; i < packet.getNeigh(); i++) {
                NodeAddress otheraddr = packet.getNeighbourAddress(i);
                String other = netId + "." + otheraddr.toString();
                if (getNode(other) == null) {
                    Node tmp = addNode(other);
                    setupNode(tmp, 0, now, netId, otheraddr);
                }

                int newLen = 255 - packet.getNeighbourWeight(i);
                String edgeId = other + "-" + fullNodeId;
                Edge edge = addEdge(edgeId, other, node.getId(), true);
                setupEdge(edge, newLen);
            }
            modified = true;

        } else {
            updateNode(node, batt, now);
            Set<Edge> oldEdges = new HashSet<>();
            for (Edge e : node.getEnteringEdgeSet()) {
                oldEdges.add(e);
            }

            for (int i = 0; i < packet.getNeigh(); i++) {
                NodeAddress otheraddr = packet.getNeighbourAddress(i);
                String other = netId + "." + otheraddr.toString();
                if (getNode(other) == null) {
                    Node tmp = addNode(other);
                    setupNode(tmp, 0, now, netId, otheraddr);
                }

                int newLen = 255 - packet.getNeighbourWeight(i);

                String edgeId = other + "-" + fullNodeId;
                Edge edge = getEdge(edgeId);
                if (edge != null) {
                    oldEdges.remove(edge);
                    int oldLen = edge.getAttribute("length");
                    if (Math.abs(oldLen - newLen) > rssiResolution) {
                        updateEdge(edge, newLen);
                        modified = true;
                    }
                } else {
                    Edge tmp = addEdge(edgeId, other, node.getId(), true);
                    setupEdge(tmp, newLen);
                    modified = true;
                }
            }

            if (!oldEdges.isEmpty()) {
                for (Edge e : oldEdges) {
                    removeEdge(e);
                }
                modified = true;
            }
        }

        if (modified) {
            lastModification++;
            setChanged();
            notifyObservers();
        }
    }

    final boolean isAlive(long threashold, long lastSeen, long now) {
        return ((now - lastSeen) < threashold * 1000);
    }

    void setupNode(Node node, int batt, long now, int netId, NodeAddress addr) {
        node.addAttribute("battery", batt);
        node.addAttribute("lastSeen", now);
        node.addAttribute("netId", netId);
        node.addAttribute("nodeAddress", addr);
    }

    void updateNode(Node node, int batt, long now) {
        node.addAttribute("battery", batt);
        node.addAttribute("lastSeen", now);
    }

    void setupEdge(Edge edge, int newLen) {
        edge.addAttribute("length", newLen);
    }

    void updateEdge(Edge edge, int newLen) {
        edge.addAttribute("length", newLen);
    }

    <T extends Node> T addNode(String id) {
        return graph.addNode(id);
    }

    <T extends Edge> T addEdge(String id, String from, String to,
            boolean directed) {
        return graph.addEdge(id, from, to, directed);
    }

    <T extends Edge> T removeEdge(Edge edge) {
        return graph.removeEdge(edge);
    }

    <T extends Node> T removeNode(Node node) {
        return graph.removeNode(node);
    }

    /**
     * Getter Method to obtain a Node of Graph.
     *
     * @param <T> the type of node in the graph.
     * @param id string id value to get a Node.
     * @return
     */
    public <T extends Node> T getNode(String id) {
        return graph.getNode(id);
    }

    /**
     * Getter Method to obtain an Edge of Graph.
     *
     * @param <T> the type of edge in the graph.
     * @param id string id value to get an Edge.
     * @return
     */
    public <T extends Edge> T getEdge(String id) {
        return graph.getEdge(id);
    }

}
