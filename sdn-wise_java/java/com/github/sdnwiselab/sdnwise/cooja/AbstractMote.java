package com.github.sdnwiselab.sdnwise.cooja;

/*
 * Copyright (c) 2010, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 */
import static com.github.sdnwiselab.sdnwise.cooja.Constants.*;
import com.github.sdnwiselab.sdnwise.flowtable.*;
import com.github.sdnwiselab.sdnwise.flowtable.FunctionAction;
import static com.github.sdnwiselab.sdnwise.flowtable.SetAction.*;
import static com.github.sdnwiselab.sdnwise.flowtable.Stats.SDN_WISE_RL_TTL_PERMANENT;
import static com.github.sdnwiselab.sdnwise.flowtable.Window.*;
import com.github.sdnwiselab.sdnwise.function.FunctionInterface;
import com.github.sdnwiselab.sdnwise.packet.*;
import static com.github.sdnwiselab.sdnwise.packet.ConfigAcceptedIdPacket.*;
import static com.github.sdnwiselab.sdnwise.packet.ConfigFunctionPacket.*;
import static com.github.sdnwiselab.sdnwise.packet.ConfigNodePacket.*;
import static com.github.sdnwiselab.sdnwise.packet.ConfigRulePacket.*;
import static com.github.sdnwiselab.sdnwise.packet.ConfigTimerPacket.*;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.*;
import com.github.sdnwiselab.sdnwise.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.*;
import org.contikios.cooja.*;
import org.contikios.cooja.interfaces.*;
import org.contikios.cooja.motes.AbstractApplicationMote;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.nio.charset.Charset;

/**
 * Example SdnWise mote.
 *
 * This mote is simulated in COOJA via the Imported App Mote Type.
 *
 * @author Sebastiano Milardo
 */
public abstract class AbstractMote extends AbstractApplicationMote {

    public ArrayList<Integer> statusRegister = new ArrayList<>();

    private Simulation simulation = null;
    private Random random = null;

    private int sentBytes;
    private int receivedBytes;

    private int sentDataBytes;
    private int receivedDataBytes;
    private int distanceFromSink;
    private int rssiSink;

    private int cntBeacon = 0;
    private int cntReport = 0;
    private int cntUpdTable = 0;

    ApplicationRadio radio = null;
    ApplicationLED leds = null;
    private EventHandler evtHandler = null;

    final ArrayBlockingQueue<NetworkPacket> flowTableQueue = new ArrayBlockingQueue<>(100);
    final ArrayBlockingQueue<NetworkPacket> txQueue = new ArrayBlockingQueue<>(100);



    int port,
            semaphore,
            flow_table_free_pos,
            accepted_id_free_pos,         //valor = 0
            neighbors_number,
            net_id,
            cnt_beacon_max,
            cnt_report_max,
            cnt_updtable_max,
            cnt_sleep_max,
            ttl_max,
            rssi_min;

    NodeAddress addr;
    DatagramSocket socket;
    Battery battery = new Battery();
    ArrayList<Neighbor> neighborTable = new ArrayList<>(100);
    ArrayList<FlowTableEntry> flowTable = new ArrayList<>(100);
    ArrayList<NodeAddress> acceptedId = new ArrayList<>(100);

    HashMap<String, Object> adcRegister = new HashMap<>();
    HashMap<Integer, LinkedList<int[]>> functionBuffer = new HashMap<>();
    HashMap<Integer, FunctionInterface> functions = new HashMap<>();
    Logger MeasureLOGGER;

/*
    // packet types
    public final static byte SDN_WISE_DATA = 0,
            SDN_WISE_BEACON = 1,
            SDN_WISE_REPORT = 2,
            SDN_WISE_REQUEST = 3,
            SDN_WISE_RESPONSE = 4,
            SDN_WISE_OPEN_PATH = 5,
            SDN_WISE_CONFIG = 6,
            SDN_WISE_DPID_CONNECTION = 7,
            SDN_WISE_MULTICAST_GROUP_JOIN = 8,
            SDN_WISE_MULTICAST_GROUP_LEAVE = 9,
            SDN_WISE_GEO_DATA = 10,
            SDN_WISE_GEO_COORDINATES = 11,
            SDN_WISE_GEO_REPORT = 12;
*/

    public AbstractMote() {
        super();
    }

    public AbstractMote(MoteType moteType, Simulation simulation) {
        super(moteType, simulation);
    }

    @Override
    public void execute(long time) {

        if (radio == null) {
            setup();                    // new Thread(new PacketManager()).start();   new Thread(new PacketSender()).start();
        }

        int delay = random.nextInt(10);

        simulation.scheduleEvent(                       //scheduleEvent é um evento do contikios
                new MoteTimeEvent(this, 0) {
                    @Override
                    public void execute(long t) {
                        timerTask();
                        logTask();
                        eventTask();  //verificar eventos
                    }
                },
                simulation.getSimulationTime() + (1000 + delay) * Simulation.MILLISECOND
        );
    }

// Metodo para geracao/tratamento de eventos

    public void eventTask(){
        if(evtHandler != null) evtHandler.executeEvent();
    }

    public EventHandler getEvent(){
      return evtHandler;
    }

    public void setEvent(EventHandler event){
      evtHandler = event;
    }

//fim geracao/tratamento de eventos

    @Override
    public void receivedPacket(RadioPacket p) {
        byte[] packetData = p.getPacketData();
        NetworkPacket np = new NetworkPacket(packetData);

        // recebe o pacote e verifica se o destino é broadcast ou se o proximo salto é o addr ou se na matriz de IDs
        // aceitos contem o "endereço do" proximo salto.

        if (np.getDst().isBroadcast()
                || np.getNxhop().equals(addr)
                || acceptedId.contains(np.getNxhop())) {      // acceptedId - matriz de Ids aceitos

            rxHandler(new NetworkPacket(packetData), 255);        // rxHandler é o FWD - faz parte
        }
    }

    @Override
    public void sentPacket(RadioPacket p) {
    }

    @Override
    public String toString() {
        return "SDN-WISE Mote " + getID();
    }

    public final void setDistanceFromSink(int num_hop_vs_sink) {
        this.distanceFromSink = num_hop_vs_sink;
    }

    public final void setRssiSink(int rssi_vs_sink) {
        this.rssiSink = rssi_vs_sink;
    }

    public final void setSemaphore(int semaforo) {
        this.semaphore = semaforo;
    }

    public final int getDistanceFromSink() {
        return distanceFromSink;
    }

    public final int getRssiSink() {
        return rssiSink;
    }

    public void initSdnWise() {

        cnt_beacon_max = SDN_WISE_DFLT_CNT_BEACON_MAX;      // definido com valor 10
        cnt_report_max = SDN_WISE_DFLT_CNT_REPORT_MAX;      // definido com valor 2 * SDN_WISE_DFLT_CNT_BEACON_MAX
        cnt_updtable_max = SDN_WISE_DFLT_CNT_UPDTABLE_MAX;  // definido com valor 6
        rssi_min = SDN_WISE_DFLT_RSSI_MIN;                  // definido com valor 1
        ttl_max = SDN_WISE_DFLT_TTL_MAX;                    // não encontrei o valor da constante

        battery = new Battery();
        flow_table_free_pos = 1;
        accepted_id_free_pos = 0;
    }

/*
  public static final byte DATA = 0,
            BEACON = 1,
            REPORT = 2,
            REQUEST = 3,
            RESPONSE = 4,
            OPEN_PATH = 5,
            CONFIG = 6,
            REG_PROXY = 7;
*/


    public final void radioTX(final NetworkPacket np) {
        sentBytes += np.getLen();
        if (np.getType() > 7 && !np.isRequest()) {      // > 7 (pode ser qualquer pacote) & não é um pacote de request
            sentDataBytes += np.getPayloadSize();     //tamanho do payload do pacote
        }

        battery.transmitRadio(np.getLen());
        np.decrementTtl();
        RadioPacket pk = new COOJARadioPacket(np.toByteArray());

        if (radio.isTransmitting() || radio.isReceiving()) {
            simulation.scheduleEvent(
                    new MoteTimeEvent(this, 0) {
                        @Override
                        public void execute(long t) {
                            radioTX(np);
                        }
                    },
                    simulation.getSimulationTime()
                            + 1 * Simulation.MILLISECOND
            );
        } else {
            radio.startTransmittingPacket(pk, 1 * Simulation.MILLISECOND);
        }
    }

    public final NodeAddress getNextHopVsSink() {
        return ((AbstractForwardAction) (flowTable.get(0).getActions().get(0))).getNextHop();
    }

    public final void rxData(DataPacket packet) {       //nesse momento recebe a msg do controlador
        if (isAcceptedIdPacket(packet)) {
            SDN_WISE_Callback(packet);
        } else if (isAcceptedIdAddress(packet.getNxhop())) {
            runFlowMatch(packet);
        }
    }

    public void rxBeacon(BeaconPacket bp, int rssi) {
        int index = getNeighborIndex(bp.getSrc());

        if (index != (SDN_WISE_NEIGHBORS_MAX + 1)) {
            if (index != -1) {
                neighborTable.get(index).setRssi(rssi);
                neighborTable.get(index).setBatt(bp.getBatt());
            } else {
                neighborTable.get(neighbors_number).setAddr(bp.getSrc());
                neighborTable.get(neighbors_number).setRssi(rssi);
                neighborTable.get(neighbors_number).setBatt(bp.getBatt());
                neighbors_number++;
            }
        }
    }

    public final void runFlowMatch(NetworkPacket packet) {      //  FWD - faz o match das regras ArrayList<FlowTableEntry> flowTable = new ArrayList<>(100);
        int j, i, found = 0;
        for (j = 0; j < SDN_WISE_RLS_MAX; j++) {          //SDN_WISE_RLS_MAX = 10000
            i = getActualFlowIndex(j);
            if (matchRule(flowTable.get(i), packet) == 1) {
                //log("Matched Rule #" + j + " " + flowTable.get(i).toString());
                found = 1;
                for (AbstractAction a : flowTable.get(i).getActions()) {
                    runAction(a, packet);
                }
                flowTable.get(i).getStats()
                        .setCounter(flowTable.get(i).getStats().getCounter() + 1);
                break;
            }
        }
        if (found == 0) { //!found
            // It's necessary to send a rule/request if we have done the lookup
            // I must modify the source address with myself,
            packet.setSrc(addr)
                    .setRequestFlag()
                    .setTtl(SDN_WISE_DFLT_TTL_MAX);
            controllerTX(packet);
        }
    }

    public abstract void rxConfig(ConfigPacket packet);

    public NodeAddress getActualSinkAddress() {
        return new NodeAddress(flowTable.get(0).getWindows().get(0).getRhs());
    }

    public abstract void SDN_WISE_Callback(DataPacket packet);

    public abstract void controllerTX(NetworkPacket pck);

    public int marshalPacket(ConfigPacket packet) {

        int toBeSent = 0;
        int pos;
        boolean isWrite = packet.isWrite();
        int id = packet.getConfigId();
        int value = packet.getPayloadAt(1) * 256 + packet.getPayloadAt(2);

        if (isWrite) {
            switch (id) {
                case SDN_WISE_CNF_ID_ADDR:
                    addr = new NodeAddress(value);
                    break;
                case SDN_WISE_CNF_ID_NET_ID:
                    net_id = packet.getPayloadAt(2);
                    break;
                case SDN_WISE_CNF_ID_CNT_BEACON_MAX:
                    cnt_beacon_max = value;
                    break;
                case SDN_WISE_CNF_ID_CNT_REPORT_MAX:
                    cnt_report_max = value;
                    break;
                case SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX:
                    cnt_updtable_max = value;
                    break;
                case SDN_WISE_CNF_ID_CNT_SLEEP_MAX:
                    cnt_sleep_max = value;
                    break;
                case SDN_WISE_CNF_ID_TTL_MAX:
                    ttl_max = packet.getPayloadAt(2);
                    break;
                case SDN_WISE_CNF_ID_RSSI_MIN:
                    rssi_min = packet.getPayloadAt(2);
                    break;
                case SDN_WISE_CNF_ADD_ACCEPTED:
                    pos = searchAcceptedId(new NodeAddress(value));
                    if (pos == (SDN_WISE_ACCEPTED_ID_MAX + 1)) {
                        pos = searchAcceptedId(new NodeAddress(65535));
                        acceptedId.set(pos, new NodeAddress(value));
                    }
                    break;
                case SDN_WISE_CNF_REMOVE_ACCEPTED:
                    pos = searchAcceptedId(new NodeAddress(value));
                    if (pos != (SDN_WISE_ACCEPTED_ID_MAX + 1)) {
                        acceptedId.set(pos, new NodeAddress(65535));
                    }
                    break;
                case SDN_WISE_CNF_REMOVE_RULE_INDEX:
                    if (value != 0) {
                        flowTable.set(getActualFlowIndex(value), new FlowTableEntry());
                    }
                    break;
                case SDN_WISE_CNF_REMOVE_RULE:
                    //TODO
                    break;
                case SDN_WISE_CNF_ADD_FUNCTION:
                    if (functionBuffer.get(value) == null) {
                        functionBuffer.put(value, new LinkedList<int[]>());
                    }
                    functionBuffer.get(value).add(Arrays.copyOfRange(
                            packet.toIntArray(), SDN_WISE_DFLT_HDR_LEN + 5,
                            packet.getLen()));
                    if (functionBuffer.get(value).size() == packet.getPayloadAt(4)) {
                        int total = 0;
                        for (int[] n : functionBuffer.get(value)) {
                            total += (n.length);
                        }
                        int pointer = 0;
                        byte[] func = new byte[total];
                        for (int[] n : functionBuffer.get(value)) {
                            for (int j = 0; j < n.length; j++) {
                                func[pointer] = (byte) n[j];
                                pointer++;
                            }
                        }
                        functions.put(value, createServiceInterface(func));       //verificar esse metodo
                        log("New Function Added at position: " + value);
                        functionBuffer.remove(value);
                    }
                    break;
                case SDN_WISE_CNF_REMOVE_FUNCTION:
                    functions.remove(value);
                    break;
                default:
                    break;
            }
        } else {
            toBeSent = 1;
            switch (id) {
                case SDN_WISE_CNF_ID_ADDR:
                    packet.setPayloadAt(addr.getHigh(), 1);
                    packet.setPayloadAt(addr.getLow(), 2);
                    break;
                case SDN_WISE_CNF_ID_NET_ID:
                    packet.setPayloadAt((byte) net_id, 2);
                    break;
                case SDN_WISE_CNF_ID_CNT_BEACON_MAX:
                    packet.setPayloadAt((byte) (cnt_beacon_max >> 8), 1);
                    packet.setPayloadAt((byte) (cnt_beacon_max), 2);
                    break;
                case SDN_WISE_CNF_ID_CNT_REPORT_MAX:
                    packet.setPayloadAt((byte) (cnt_report_max >> 8), 1);
                    packet.setPayloadAt((byte) (cnt_report_max), 2);
                    break;
                case SDN_WISE_CNF_ID_CNT_UPDTABLE_MAX:
                    packet.setPayloadAt((byte) (cnt_updtable_max >> 8), 1);
                    packet.setPayloadAt((byte) (cnt_updtable_max), 2);
                    break;
                case SDN_WISE_CNF_ID_CNT_SLEEP_MAX:
                    packet.setPayloadAt((byte) (cnt_sleep_max >> 8), 1);
                    packet.setPayloadAt((byte) (cnt_sleep_max), 2);
                    break;
                case SDN_WISE_CNF_ID_TTL_MAX:
                    packet.setPayloadAt((byte) ttl_max, 2);
                    break;
                case SDN_WISE_CNF_ID_RSSI_MIN:
                    packet.setPayloadAt((byte) rssi_min, 2);
                    break;
                case SDN_WISE_CNF_LIST_ACCEPTED:
                    toBeSent = 0;
                    ConfigAcceptedIdPacket packetList = new ConfigAcceptedIdPacket(
                                    net_id,
                                    packet.getDst(),
                                    packet.getSrc());
                    packetList.setReadAcceptedAddressesValue();
                    int ii = 1;

                    for (int jj = 0; jj < SDN_WISE_ACCEPTED_ID_MAX; jj++) {
                        if (!acceptedId.get(jj).equals(new NodeAddress(65535))) {
                            packetList.setPayloadAt((acceptedId.get(jj)
                                    .getHigh()), ii);
                            ii++;
                            packetList.setPayloadAt((acceptedId.get(jj)
                                    .getLow()), ii);
                            ii++;
                        }
                    }
                    controllerTX(packetList);
                    break;
                case SDN_WISE_CNF_GET_RULE_INDEX:
                    toBeSent = 0;
                    ConfigRulePacket packetRule = new ConfigRulePacket(
                            net_id,
                            packet.getDst(),
                            packet.getSrc()
                    );
                    int jj = getActualFlowIndex(value);
                    packetRule.setRule(flowTable.get(jj))
                            .setPayloadAt(SDN_WISE_CNF_GET_RULE_INDEX, 0)
                            .setPayloadAt(packet.getPayloadAt(1), 1)
                            .setPayloadAt(packet.getPayloadAt(2), 2);
                    controllerTX(packetRule);
                    break;
                default:
                    break;
            }
        }
        return toBeSent;
    }

    private void timerTask() {
        if (semaphore == 1 && battery.getBatteryLevel() > 0) {
            battery.keepAlive(1);

            cntBeacon++;
            cntReport++;
            cntUpdTable++;

            if ((cntBeacon) >= cnt_beacon_max) {
                cntBeacon = 0;
                radioTX(prepareBeacon());
            }

            if ((cntReport) >= cnt_report_max) {
                cntReport = 0;
                controllerTX(prepareReport());
            }

            if ((cntUpdTable) >= cnt_updtable_max) {
                cntUpdTable = 0;
                updateTable();
            }
        }
        requestImmediateWakeup();
    }

    private void initFlowTable() {
        FlowTableEntry toSink = new FlowTableEntry();
        toSink.addWindow(new Window()
                .setOperator(SDN_WISE_EQUAL)
                .setSize(SDN_WISE_SIZE_2)
                .setLhsLocation(SDN_WISE_PACKET)
                .setLhs(SDN_WISE_DST_H)
                .setRhsLocation(SDN_WISE_CONST)
                .setRhs(this.addr.intValue()));
        toSink.addWindow(Window.fromString("P.TYPE > 127"));
        toSink.addAction(new ForwardUnicastAction()
                .setNextHop(addr));
        toSink.getStats().setPermanent();
        flowTable.add(0, toSink);

        for (int i = 1; i < SDN_WISE_RLS_MAX; i++) {        //SDN_WISE_RLS_MAX = 10000
            flowTable.add(i, new FlowTableEntry());
        }
    }

    private void rxReport(ReportPacket packet) {

      //  log("battery: " + packet.getBatt() + " Mote: " + packet.getSrc());
        controllerTX(packet);
    }

    //carrega a classe e instancia o obj em tempo de execução

    private FunctionInterface createServiceInterface(byte[] classFile) {   //pegando o array de bytes e transformando em instancia - instanciar um obj
        CustomClassLoader cl = new CustomClassLoader();
        FunctionInterface srvI = null;
        Class service = cl.defClass(classFile, classFile.length);
        try {
            srvI = (FunctionInterface) service.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            log(ex.getLocalizedMessage());
        }
        return srvI;
    }


    private void rxResponse(ResponsePacket rp) {
        if (isAcceptedIdPacket(rp)) {
            rp.getRule().setStats(new Stats());
            insertRule(rp.getRule(), searchRule(rp.getRule()));
        } else {
            runFlowMatch(rp);
        }
    }

    private void rxOpenPath(OpenPathPacket opp) {
        if (isAcceptedIdPacket(opp)) {
            List<NodeAddress> path = opp.getPath();
            for (int i = 0; i < path.size(); i++) {
                NodeAddress actual = path.get(i);
                if (isAcceptedIdAddress(actual)) {
                    if (i != 0) {
                        FlowTableEntry rule = new FlowTableEntry();
                        rule.addWindow(new Window()
                                .setOperator(SDN_WISE_EQUAL)
                                .setSize(SDN_WISE_SIZE_2)
                                .setLhsLocation(SDN_WISE_PACKET)
                                .setLhs(SDN_WISE_DST_H)
                                .setRhsLocation(SDN_WISE_CONST)
                                .setRhs(path.get(0).intValue()));
                        rule.getWindows().addAll(opp.getOptionalWindows());
                        rule.addAction(new ForwardUnicastAction()
                                .setNextHop(path.get(i - 1))
                        );
                        int p = searchRule(rule);
                        insertRule(rule, p);
                    }

                    if (i != (path.size() - 1)) {
                        FlowTableEntry rule = new FlowTableEntry();
                        rule.addWindow(new Window()
                                .setOperator(SDN_WISE_EQUAL)
                                .setSize(SDN_WISE_SIZE_2)
                                .setLhsLocation(SDN_WISE_PACKET)
                                .setLhs(SDN_WISE_DST_H)
                                .setRhsLocation(SDN_WISE_CONST)
                                .setRhs(path.get(path.size() - 1).intValue()));

                        rule.getWindows().addAll(opp.getOptionalWindows());
                        rule.addAction(new ForwardUnicastAction()
                                .setNextHop(path.get(i + 1))
                        );

                        int p = searchRule(rule);
                        insertRule(rule, p);
                        opp.setDst(path.get(i + 1));
                        opp.setNxhop(path.get(i + 1));

                        radioTX(opp);
                        break;
                    }
                }
            }
        } else {
            runFlowMatch(opp);
        }
    }

    private void insertRule(FlowTableEntry rule, int pos) {
        if (pos >= SDN_WISE_RLS_MAX) {                          //SDN_WISE_RLS_MAX = 10000
            pos = flow_table_free_pos;                          //inicialmente = 1
            flow_table_free_pos++;
            if (flow_table_free_pos >= SDN_WISE_RLS_MAX) {      //SDN_WISE_RLS_MAX = 10000
                flow_table_free_pos = 1;
            }
        }
        log("Inserting rule " + rule + " at position " + pos);
        flowTable.set(pos, rule);
    }

    private int searchRule(FlowTableEntry rule) {
        int i, j, sum, target;
        for (i = 0; i < SDN_WISE_RLS_MAX; i++) {                        //SDN_WISE_RLS_MAX = 10000
            sum = 0;
            target = rule.getWindows().size();
            if (flowTable.get(i).getWindows().size() == target) {
                for (j = 0; j < rule.getWindows().size(); j++) {
                    if (flowTable.get(i).getWindows().get(j).equals(rule.getWindows().get(j))) {
                        sum++;
                    }
                }
            }
            if (sum == target) {
                return i;
            }
        }
        return SDN_WISE_RLS_MAX + 1;                  //SDN_WISE_RLS_MAX = 10000
    }

    private boolean isAcceptedIdAddress(NodeAddress addrP) {
        return (addrP.equals(addr)
                || addrP.isBroadcast()
                || (searchAcceptedId(addrP)
                != SDN_WISE_ACCEPTED_ID_MAX + 1));          //SDN_WISE_ACCEPTED_ID_MAX = 10;
    }

    private boolean isAcceptedIdPacket(NetworkPacket packet) {
        return isAcceptedIdAddress(packet.getDst());
    }

    private void rxHandler(NetworkPacket packet, int rssi) { // CAMADA FWD - trata os pacotes conforme tabela de fluxo!

      // se não entrar nesse if, sai do método
      //rxBeacon(new BeaconPacket(packet), rssi);
        if (packet.getLen() > SDN_WISE_DFLT_HDR_LEN   // SDN_WISE_DFLT_HDR_LEN = 10 [cabeçalho]- Constants.java
                && packet.getNetId() == net_id        //ned_id = 1
                && packet.getTtl() != 0) {            //ttl definido como TTL_INDEX = 7 em NetworkPacket

            if (packet.isRequest()) {    //Pacote request - encapsula um pacote que não tem correspondência em uma tabela de fluxo.
                controllerTX(packet);
            } else {
                switch (packet.getType()) {
                    //Pacote composto apenas pelo cabeçalho e payload;
                    case SDN_WISE_DATA:

                        rxData(new DataPacket(packet));
                        break;

                        //Transmitido através de broadcast, utilizado para que os nodos compartilhem
                        //entre si informações de bateria e distância
                        //do sorvedouro;
                    case SDN_WISE_BEACON:
                        rxBeacon(new BeaconPacket(packet), rssi);
                        break;

                      //Utilizado para manter o controlador atualizado sobre o estado dos enlaces na rede.
                      //O pacote, transmitido pelos nodos da rede, é composto por informações da vizinhança
                      //(endereço e indicador Received signal strength indication (RSSI)), além da sua
                      //distância do sorvedouro e nível de bateria.
                    case SDN_WISE_REPORT:
                        rxReport(new ReportPacket(packet));
                        break;

                      //Pacote transmitido pelo controlador que contém a resposta à requisição de uma nova regra de correspondência.
                    case SDN_WISE_RESPONSE:
                        rxResponse(new ResponsePacket(packet));
                        break;

                    /*
                    Este tipo de pacote tem como finalidade diminuir a quantidade de pacotes transmitidos na rede.
                    Pois ao invés de enviar uma regra de roteamento para cada nodo, o controlador cria um
                    caminho de roteamento e transmite, neste pacote, os endereços de todos os nodos que compõem o
                    caminho. Ao receber o pacote, os nodos criam regras de correspondências com base nos endereços
                    dos nodos que compõem o caminho.
                    */
                    case SDN_WISE_OPEN_PATH:
                        rxOpenPath(new OpenPathPacket(packet));
                        break;

                    // Utilizado na configuração da rede
                    case SDN_WISE_CONFIG:
                        rxConfig(new ConfigPacket(packet));
                        break;

                    default:
                        runFlowMatch(packet);
                        break;
                }
            }
        }
    }

    private void initNeighborTable() {
        int i;
        for (i = 0; i < SDN_WISE_NEIGHBORS_MAX; i++) {
            neighborTable.add(i, new Neighbor());
        }
        neighbors_number = 0;
    }

    private void initStatusRegister() {
        for (int i = 0; i < SDN_WISE_STATUS_LEN; i++) {
            statusRegister.add(0);
        }
    }

    private void initAcceptedId() {
        for (int i = 0; i < SDN_WISE_ACCEPTED_ID_MAX; i++) {
            acceptedId.add(i, new NodeAddress(65535));
        }
    }

    private void setup() {

        addr = new NodeAddress(this.getID());
        net_id = (byte) 1;

        simulation = getSimulation();
        random = simulation.getRandomGenerator();
        radio = (ApplicationRadio) getInterfaces().getRadio();
        leds = (ApplicationLED) getInterfaces().getLED();
        MeasureLOGGER = Logger.getLogger("Measure" + addr.toString());
        MeasureLOGGER.setLevel(Level.parse("FINEST"));

        try {
            FileHandler fh;
            File dir = new File("logs");
            dir.mkdir();
            fh = new FileHandler("logs/Measures" + addr + ".log");
            fh.setFormatter(new SimplestFormatter());
            MeasureLOGGER.addHandler(fh);
            MeasureLOGGER.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            log(ex.getLocalizedMessage());
        }
        neighborTable = new ArrayList<>(SDN_WISE_NEIGHBORS_MAX);
        acceptedId = new ArrayList<>(SDN_WISE_ACCEPTED_ID_MAX);
        flowTable = new ArrayList<>(50);

        initFlowTable();
        initNeighborTable();
        initAcceptedId();
        initStatusRegister();
        initSdnWise();

        new Thread(new PacketManager()).start();
        new Thread(new PacketSender()).start();
    }

    private int getOperand(NetworkPacket packet, int size, int location, int value) {
        int[] intPacket = packet.toIntArray();
        switch (location) {
            case SDN_WISE_NULL:
                return 0;
            case SDN_WISE_CONST:
                return value;
            case SDN_WISE_PACKET:
                if (size == SDN_WISE_SIZE_1) {
                    if (value >= intPacket.length) {
                        return -1;
                    }
                    return intPacket[value];
                }
                if (size == SDN_WISE_SIZE_2) {
                    if (value + 1 >= intPacket.length) {
                        return -1;
                    }
                    return Utils.mergeBytes(intPacket[value], intPacket[value + 1]);
                }
            case SDN_WISE_STATUS:
                if (size == SDN_WISE_SIZE_1) {
                    if (value >= statusRegister.size()) {
                        return -1;
                    }
                    return statusRegister.get(value);
                }
                if (size == SDN_WISE_SIZE_2) {
                    if (value + 1 >= statusRegister.size()) {
                        return -1;
                    }
                    return Utils.mergeBytes(
                            statusRegister.get(value),
                            statusRegister.get(value + 1));
                }
        }
        return -1;
    }
/**
Cada com.github.sdnwiselab.sdnwise.flowtable.Window permite definir uma condição a ser verificada
para a execução das ações. Uma condição é composta por três partes, um lado esquerdo, um lado direito e um operador.
É possível especificar a localização do operando (usando o método setLhsLocation e setRhsLocation)
e o endereço (usando o método setLhs ou setRhs).
**/

	private int matchWindow(Window window, NetworkPacket packet) {   //Window representa uma condição a ser verificada na Tabela de Fluxo
        int operator = window.getOperator();
        int size = window.getSize();
        int lhs = getOperand(
                packet, size, window.getLhsLocation(), window.getLhs());
        int rhs = getOperand(
                packet, size, window.getRhsLocation(), window.getRhs());
        return compare(operator, lhs, rhs);  //Uma condição é composta por três partes, um lado esquerdo, um lado direito e um operador.
    }

    private int matchRule(FlowTableEntry rule, NetworkPacket packet) {
        if (rule.getWindows().isEmpty()) {
            return 0;
        }

        int target = rule.getWindows().size();
        int actual = 0;

        for (Window w : rule.getWindows()) {
            actual = actual + matchWindow(w, packet);
        }
        return (actual == target ? 1 : 0);
    }

    private void runAction(AbstractAction action, NetworkPacket np) {
        try {
            int action_type = action.getType();

            switch (action_type) {
                case SDN_WISE_FORWARD_U:
                case SDN_WISE_FORWARD_B:
                    np.setNxhop(((AbstractForwardAction) action).getNextHop());
                    radioTX(np);
                    break;

                case SDN_WISE_DROP:
                    break;
                case SDN_WISE_SET:
                    SetAction ftam = (SetAction) action;
                    int operator = ftam.getOperator();
                    int lhs = getOperand(
                            np, SDN_WISE_SIZE_1, ftam.getLhsLocation(), ftam.getLhs());
                    int rhs = getOperand(
                            np, SDN_WISE_SIZE_1, ftam.getRhsLocation(), ftam.getRhs());
                    if (lhs == -1 || rhs == -1) {
                        throw new IllegalArgumentException("Operators out of bound");
                    }
                    int res = doOperation(operator, lhs, rhs);
                    if (ftam.getResLocation() == SDN_WISE_PACKET) {
                        int[] packet = np.toIntArray();
                        if (ftam.getRes() >= packet.length) {
                            throw new IllegalArgumentException("Result out of bound");
                        }
                        packet[ftam.getRes()] = res;
                        np.setArray(packet);
                    } else {
                        statusRegister.set(ftam.getRes(), res);
                        log("SET R." + ftam.getRes() + " = " + res + ". Done.");
                    }
                    break;
                case SDN_WISE_FUNCTION:
                    FunctionAction ftac = (FunctionAction) action;
                    FunctionInterface srvI = functions.get(ftac.getCallbackId());
                    //FunctionInterface srvI = functions.get(1);
                    if (srvI != null) {
                        log("Function called: " + addr);
                        srvI.function(adcRegister,
                                flowTable,
                                neighborTable,
                                statusRegister,
                                acceptedId,
                                flowTableQueue,
                                txQueue,
                                ftac.getArg0(),
                                ftac.getArg1(),
                                ftac.getArg2(),
                                np
                        );
                    }
                    break;
                case SDN_WISE_ASK:
                    np.setSrc(addr)
                            .setRequestFlag()
                            .setTtl(NetworkPacket.SDN_WISE_DFLT_TTL_MAX);
                    controllerTX(np);
                    break;
                case SDN_WISE_MATCH:
                    flowTableQueue.add(np);
                    break;
                case SDN_WISE_TO_UDP:
                    ToUdpAction tua = (ToUdpAction) action;
                    DatagramSocket sUDP = new DatagramSocket();
                    DatagramPacket pck = new DatagramPacket(np.toByteArray(),
                            np.getLen(), tua.getInetSocketAddress());
                    sUDP.send(pck);
                    break;
                default:
                    break;
            }//switch
        } catch (IOException ex) {
            log(ex.getLocalizedMessage());
        }
    }

    private int doOperation(int operatore, int item1, int item2) {
        switch (operatore) {
            case SDN_WISE_ADD:
                return item1 + item2;
            case SDN_WISE_SUB:
                return item1 - item2;
            case SDN_WISE_DIV:
                return item1 / item2;
            case SDN_WISE_MUL:
                return item1 * item2;
            case SDN_WISE_MOD:
                return item1 % item2;
            case SDN_WISE_AND:
                return item1 & item2;
            case SDN_WISE_OR:
                return item1 | item2;
            case SDN_WISE_XOR:
                return item1 ^ item2;
            default:
                return 0;
        }
    }

    private int compare(int operatore, int item1, int item2) {
        if (item1 == -1 || item2 == -1) {
            return 0;
        }
        switch (operatore) {
            case SDN_WISE_EQUAL:
                return item1 == item2 ? 1 : 0;
            case SDN_WISE_NOT_EQUAL:
                return item1 != item2 ? 1 : 0;
            case SDN_WISE_BIGGER:
                return item1 > item2 ? 1 : 0;
            case SDN_WISE_LESS:
                return item1 < item2 ? 1 : 0;
            case SDN_WISE_EQUAL_OR_BIGGER:
                return item1 >= item2 ? 1 : 0;
            case SDN_WISE_EQUAL_OR_LESS:
                return item1 <= item2 ? 1 : 0;
            default:
                return 0;
        }
    }

    void resetSemaphore() {
    }

    BeaconPacket prepareBeacon() {
        BeaconPacket bp = new BeaconPacket(
                net_id,
                addr,
                getActualSinkAddress(),
                distanceFromSink,
                battery.getBatteryPercent());
        return bp;
    }

    ReportPacket prepareReport() {

        ReportPacket rp = new ReportPacket(
                net_id,
                addr,
                getActualSinkAddress(),
                distanceFromSink,
                battery.getBatteryPercent());

        rp.setNeigh(neighbors_number)
                .setNxhop(getNextHopVsSink());

        for (int j = 0; j < neighbors_number; j++) {
            rp.setNeighbourAddressAt(neighborTable.get(j).getAddr(), j)
                    .setNeighbourWeightAt((byte) neighborTable.get(j).getRssi(), j);
        }
        initNeighborTable();
        return rp;
    }

    final void updateTable() {
        for (int i = 0; i < SDN_WISE_RLS_MAX; i++) {                //SDN_WISE_RLS_MAX = 10000
            FlowTableEntry tmp = flowTable.get(i);
            if (tmp.getWindows().size() > 1) {
                int ttl = tmp.getStats().getTtl();
                if (ttl != SDN_WISE_RL_TTL_PERMANENT) {
                    if (ttl >= SDN_WISE_RL_TTL_DECR) {
                        tmp.getStats().decrementTtl(SDN_WISE_RL_TTL_DECR);
                    } else {

                        flowTable.set(i, new FlowTableEntry());
                        log("Removing rule at position " + i);
                        if (i == 0) {
                            resetSemaphore();
                        }
                    }
                }
            }
        }
    }

    final int getNeighborIndex(NodeAddress addr) {
        int i;
        for (i = 0; i < SDN_WISE_NEIGHBORS_MAX; i++) {
            if (neighborTable.get(i).getAddr().equals(addr)) {
                return i;
            }
            if (neighborTable.get(i).getAddr().isBroadcast()) {
                return -1;
            }
        }
        return SDN_WISE_NEIGHBORS_MAX + 1;
    }

    final int searchAcceptedId(NodeAddress addr) {
        int i;
        for (i = 0; i < SDN_WISE_ACCEPTED_ID_MAX; i++) {
            if (acceptedId.get(i).equals(addr)) {
                return i;
            }
        }
        return SDN_WISE_ACCEPTED_ID_MAX + 1;
    }

    final int getActualFlowIndex(int j) {
        //j = j % SDN_WISE_RLS_MAX;
        int i;
        if (j == 0) {
            i = 0;
        } else {
            i = flow_table_free_pos - j;
            if (i == 0) {
                i = SDN_WISE_RLS_MAX - 1;               //SDN_WISE_RLS_MAX = 10000
            } else if (i < 0) {
                i = SDN_WISE_RLS_MAX - 1 + i;           //SDN_WISE_RLS_MAX = 10000
            }
        }
        return i;
    }

    void logTask() {
        MeasureLOGGER.log(Level.FINEST,
                // NODE;BATTERY LVL(mC);BATTERY LVL(%);NO. RULES INSTALLED; B SENT; B RECEIVED;
                "{0},{1},{2},{3},{4},{5},{6},{7}",
                new Object[]{
                    addr,
                    String.valueOf(battery.getBatteryLevel()),
                    String.valueOf(battery.getBatteryPercent() / 2.55),
                    flow_table_free_pos,
                    sentBytes,
                    receivedBytes,
                    sentDataBytes,
                    receivedDataBytes
                });
    }

    private class CustomClassLoader extends ClassLoader {

        public Class defClass(byte[] data, int len) {
            return defineClass(null, data, 0, len);
        }
    }

    private class PacketManager implements Runnable {

        @Override
        public void run() {
            try {
                while (battery.getBatteryLevel() > 0) {
                    NetworkPacket tmpPacket = flowTableQueue.take();
                    battery.receiveRadio(tmpPacket.getLen());
                    receivedBytes += tmpPacket.getLen();
                    rxHandler(tmpPacket, 255);
                }
            } catch (InterruptedException ex) {
                log(ex.getLocalizedMessage());
            }
        }
    }

    private class PacketSender implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    NetworkPacket np = txQueue.take();
                    radioTX(np);
                }
            } catch (InterruptedException ex) {
                log(ex.getLocalizedMessage());
            }
        }
    }
}
