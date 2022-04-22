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
import com.github.sdnwiselab.sdnwise.adapter.AdapterTcp;
import com.github.sdnwiselab.sdnwise.adapter.AdapterUdp;
import com.github.sdnwiselab.sdnwise.configuration.ConfigController;
import com.github.sdnwiselab.sdnwise.topology.*;

/**
 * This class creates a Controller object given the specifications contained in
 * a ConfigController object. In the current version the only possible lower
 * adapter is an AdapterUdp while the algorithm can be Dijkstra or static.
 * <p>
 * It is also possible to specify some parameters for the network
 * representation.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class ControllerFactory {


    public Adapter getLower(ConfigController conf) {

        String type = conf.getLower().get("TYPE");
        switch (type) {
            case "TCP":
                return new AdapterTcp(conf.getLower());
            case "UDP":
                return new AdapterUdp(conf.getLower());
            default:
                throw new UnsupportedOperationException("Error in config file");
        }
    }

    public NetworkGraph getNetworkGraph(ConfigController conf) {
        String graph = conf.getMap().get("GRAPH");
        int timeout = Integer.parseInt(conf.getMap().get("TIMEOUT"));
        int rssiResolution = Integer.parseInt(conf.getMap().get("RSSI_RESOLUTION"));

        switch (graph) {
            case "CLI":
                return new NetworkGraph(timeout, rssiResolution);
            case "GUI":
                return new VisualNetworkGraph(timeout, rssiResolution);
            default:
                throw new UnsupportedOperationException("Error in Configuration file");
        }
    }

    
    public Controller getControllerType(ConfigController conf, Adapter adapt, NetworkGraph ng) {
        String type = conf.getAlgorithm().get("TYPE");

        switch (type) {
            case "DIJKSTRA":
                return new ControllerDijkstra(adapt, ng);
            default:
                throw new UnsupportedOperationException("Error in Configuration file");
        }
    }

    /**
     * Return the corresponding Controller object given a ConfigController
     * object.
     *
     * @param config a ConfigController object.
     * @return a Controller object.
     */
    public final Controller getController(ConfigController config) {
        Adapter adapt = getLower(config);
        NetworkGraph ng = getNetworkGraph(config);
        return getControllerType(config, adapt, ng);
    }
}
