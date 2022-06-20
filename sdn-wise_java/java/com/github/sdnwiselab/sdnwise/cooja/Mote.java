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
package com.github.sdnwiselab.sdnwise.cooja;

import static com.github.sdnwiselab.sdnwise.cooja.Constants.*;
import com.github.sdnwiselab.sdnwise.flowtable.*;
import static com.github.sdnwiselab.sdnwise.flowtable.Window.*;
import com.github.sdnwiselab.sdnwise.packet.*;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_H;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.nio.charset.Charset;
import org.contikios.cooja.*;

/**
 *
 * @author Sebastiano Milardo
 */
public class Mote extends AbstractMote {

    Agente agente;

    public Mote() {
        super();
        agente = new Agente();
    }

    public Mote(MoteType moteType, Simulation simulation) {
        super(moteType, simulation);
        agente = new Agente();
    }

    @Override
    public final void initSdnWise() {
        super.initSdnWise();
        commonConstructor();
        setDistanceFromSink(ttl_max + 1);
        setRssiSink(0);
        setSemaphore(0);
    }

    @Override
    public void SDN_WISE_Callback(DataPacket packet) {
        if (this.functions.get(1) == null) {
            log("Callback: " + new String(packet.getPayload(),Charset.forName("UTF-8")));
            agente.receiveController(packet, this);
            // packet.setSrc(addr)
            //         .setDst(getActualSinkAddress())
            //         .setTtl((byte) ttl_max);
            //runFlowMatch(packet);
        } else {
            this.functions.get(1).function(adcRegister,
                    flowTable,
                    neighborTable,
                    statusRegister,
                    acceptedId,
                    flowTableQueue,
                    txQueue,
                    0,
                    0,
                    0,
                    packet);
        }
    }

    @Override
    public void rxBeacon(BeaconPacket bp, int rssi) {
        if (rssi > rssi_min) {
            if (bp.getDist() < this.getDistanceFromSink()
                    && (rssi > getRssiSink())) {
                this.setSemaphore(1);
                FlowTableEntry toSink = new FlowTableEntry();
                toSink.addWindow(new Window()
                        .setOperator(SDN_WISE_EQUAL)
                        .setSize(SDN_WISE_SIZE_2)
                        .setLhsLocation(SDN_WISE_PACKET)
                        .setLhs(SDN_WISE_DST_H)
                        .setRhsLocation(SDN_WISE_CONST)
                        .setRhs(bp.getSinkAddress().intValue()));
                toSink.addWindow(Window.fromString("P.TYPE > 127"));
                toSink.addAction(new ForwardUnicastAction()
                        .setNextHop(bp.getSrc()));
                flowTable.set(0, toSink);

                setDistanceFromSink(bp.getDist() + 1);
                setRssiSink(rssi);
            } else if ((bp.getDist() + 1) == this.getDistanceFromSink()
                    && getNextHopVsSink().equals(bp.getSrc())) {
                flowTable.get(0).getStats().restoreTtl();
                flowTable.get(0).getWindows().get(0)
                        .setRhs(bp.getSinkAddress().intValue());
            }
            super.rxBeacon(bp, rssi);
        }
    }

    @Override
    public final void controllerTX(NetworkPacket pck) {
        pck.setNxhop(getNextHopVsSink());
        radioTX(pck);
    }

    @Override
    public void rxConfig(ConfigPacket packet) {
        NodeAddress dest = packet.getDst();
        if (!dest.equals(addr)) {
            runFlowMatch(packet);
        } else {
            if (this.marshalPacket(packet) != 0) {
                packet.setSrc(addr);
                packet.setDst(getActualSinkAddress());
                packet.setTtl((byte) ttl_max);
                runFlowMatch(packet);
            }
        }
    }

    private void commonConstructor() {
        cnt_sleep_max = SDN_WISE_DFLT_CNT_SLEEP_MAX;
    }

    @Override
    final void resetSemaphore() {
        setSemaphore(0);
        setDistanceFromSink(255);
    }
}
