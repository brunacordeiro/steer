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
package com.github.sdnwiselab.sdnwise.function;

import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.util.Neighbor;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Sebastiano Milardo
 */
public interface FunctionInterface {

    /**
     * This interface must be implemented by any function that will be sent to a
     * node. A node can learn new functions that will be executed by using an
     * action SDN_WISE_FORWARD_UP and the corresponding ID of the function. A
     * function can be installed in a node by using the sendFunction method of a
     * controller.
     *
     * @param adcRegister an HashMap containing measurement info.
     * @param flowTable an ArrayList containing the FlowTable of the node.
     * @param neighborTable an ArrayList containing the Neighbors table of the
     * node.
     * @param statusRegister an int[] containing the status of the node.
     * @param acceptedId an ArrayList of NodeAddress containing the aliases for
     * the address of the node.
     * @param flowTableQueue messages added in this queue will be matched in the
     * flow table.
     * @param txQueue messages added in this queue will be sent immediately.
     * @param arg1 is a parameter passed by the action.
     * @param arg2 is a parameter passed by the action.
     * @param arg3 is a parameter passed by the action.
     * @param np the NetworkPacket that triggered this function.
     */
    public void function(
            HashMap<String, Object> adcRegister,
            ArrayList<FlowTableEntry> flowTable,
            ArrayList<Neighbor> neighborTable,
            ArrayList<Integer> statusRegister,
            ArrayList<NodeAddress> acceptedId,
            ArrayBlockingQueue<NetworkPacket> flowTableQueue,
            ArrayBlockingQueue<NetworkPacket> txQueue,
            int arg1,
            int arg2,
            int arg3,
            NetworkPacket np
    );
}
