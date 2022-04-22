package com.github.sdn_idn_iot.idn;

import com.github.sdnwiselab.sdnwise.application.AbstractApplication;
import com.github.sdnwiselab.sdnwise.application.MessageHandler;
import com.github.sdnwiselab.sdnwise.application.MonitoringMetrics;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class Communication extends AbstractApplication {

    private int idMsg;
    public int auxRX;
    public int auxTX;
    private final HashMap<Integer, MessageHandler> mapa;
    private Controller ctrl;
    private MonitoringMetrics monitoringMetrics;

    public Communication(Controller ctrl) {
        super(ctrl);
        this.ctrl = ctrl;
        mapa = new HashMap<>();
        monitoringMetrics = new MonitoringMetrics();
        auxTX = 0;
        auxRX = 0;
    }

    public MonitoringMetrics getMonitoringMetrics() {
        return monitoringMetrics;
    }

    public void setMonitoringMetrics(MonitoringMetrics monitoring) {
        this.monitoringMetrics = monitoring;
    }

    public void polling(String tipoSensor, int moteId, MessageHandler msg) {
        int id = geraIdMessage();
        mapa.put(id, msg);
        String solicitacao = tipoSensor + ", " + "polling" + ", " + 0 + ", " + 0 + ", " + 0 + ", " + id + ", " + 0;
       // System.out.println("\nTamanho da string enviada POLLING: " + solicitacao.getBytes(Charset.forName("UTF-8")).length);
//      DataPacket dp = new DataPacket(netId, src, dst);
        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
        sendMessage(dp, solicitacao);
        //receiveMetrics(dp);
    }
    
    public void pollingAlerta(String tipoSensor, int moteId, MessageHandler msg) {
        int id = geraIdMessage();
        mapa.put(id, msg);
        String solicitacao = tipoSensor + ", " + "pollingAlerta" + ", " + 0 + ", " + 0 + ", " + 0 + ", " + id + ", " + 0;
       // System.out.println("\nTamanho da string enviada POLLING: " + solicitacao.getBytes(Charset.forName("UTF-8")).length);
//      DataPacket dp = new DataPacket(netId, src, dst);
        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
        sendMessage(dp, solicitacao);
        //receiveMetrics(dp);
    }

    public void eventoPeriodico(String tipoSensor, int moteId, int intervalo, MessageHandler msg) {
        int id = geraIdMessage();
        mapa.put(id, msg);
        String solicitacao = tipoSensor + ", " + "periodic" + ", " + intervalo + ", " + 0 + ", " + 0 + ", " + id + ", " + 0;
        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
        sendMessage(dp, solicitacao);
    }

//    public void alerta(String tipoSensor, int moteId, String limite, MessageHandler msg) {
//        int id = geraIdMessage();
//        mapa.put(id, msg);
//        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
//        String solicitacao = tipoSensor + ", " + "alert" + ", " + 0 + ", " + limite + ", " + id;
//        sendMessage(dp, solicitacao);
//
//    }
    
    public void alerta(String tipoSensor, int moteId, float dadoSensor, String limite, MessageHandler msg) {
        int id = geraIdMessage();
        mapa.put(id, msg);
        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
        String solicitacao = tipoSensor + ", " + "alert" + ", " + 0 + ", " + limite + ", " + id + ", " + dadoSensor;
        sendMessage(dp, solicitacao);

    }

    public void delete(String delete, int moteId, MessageHandler msg) {
        int id = geraIdMessage();
        mapa.put(id, msg);
        DataPacket dp = new DataPacket(1, new NodeAddress(1), new NodeAddress(moteId));
        String solicitacao = " " + ", " + delete + ", " + 0 + ", " + 0 + ", " + 0 + ", " + id + ", " + 0;
        sendMessage(dp, solicitacao);
    }

    public Integer geraIdMessage() {
        return idMsg++;
    }
    
    
    public void sendMessage(DataPacket dp, String solicitacao) {
        dp.setNxhop(new NodeAddress(1));
        dp.setPayload(solicitacao.getBytes(Charset.forName("UTF-8")));
        ctrl.sendNetworkPacket(dp);
    
        if(!monitoringMetrics.getCount()){
            auxTX = 0;
            auxRX = 0;
            monitoringMetrics.setCount(true);          
        } else{
            auxTX = auxTX + 1;
            monitoringMetrics.setCountMsgTX(auxTX);
           // System.out.println("send: " + auxTX);
        }
    }

    @Override
    public void receivePacket(DataPacket data) {
        String dataReceive = new String(data.getPayload(), Charset.forName("UTF-8"));
        String[] arrayDataMotes = dataReceive.split(", ");
        String mote = data.getSrc().toString();
        String m = mote.replaceAll("0.", "");
        int idMote = Integer.parseInt(m);
        mapa.get(Integer.parseInt(arrayDataMotes[2])).message(arrayDataMotes[1], idMote);  //id e msg
           
        if(!monitoringMetrics.getCount()){
            auxTX = 0;
            auxRX = 0;
            monitoringMetrics.setCount(true);
        } else{
            auxRX = auxRX + 1;
            monitoringMetrics.setCountMsgRX(auxRX);
           // System.out.println("receive: " + auxRX);
        }

    }

    @Override
    public void receiveMetrics(NetworkPacket netPacket) {
        ReportPacket rp = new ReportPacket(netPacket);
        monitoringMetrics.receiveMetrics(netPacket);
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        GregorianCalendar gc = new GregorianCalendar();
        String hour = sdf.format(gc.getTime());
        //System.out.println("Mote: " + rp.getSrc() + " Bateria - " + rp.getBatt() 
               // + " TTL - " + rp.getTtl() + " Hora: " + hour);

    }
    
} //fim mediador

/*

//        entry.addWindow(new Window()
//                .setLhsLocation(1)
//                .setLhs(11)
//                .setOperator(EQUAL)
//                .setRhsLocation(LESSER)
//                .setRhs(230));

//        
//      
//        //String entryRule = JOptionPane.showInputDialog("Rule: ", "IF (P.DST_HIGH == 1) {FORWARD_U 8}");
//        String entryRule = JOptionPane.showInputDialog("Rule: ", 
//                "IF (P.DST_HIGH == 1 && P.11 < 230) {FORWARD_U 8}");
//        //ctrl.sendFunction((byte) 1, new NodeAddress(moteId), (byte) 2, "HelloWorld.class");
//        
//
//        FlowTableEntry entry = FlowTableEntry.fromString(entryRule);
//            entry.addWindow(new Window()
//                    .setLhsLocation(SDN_WISE_PACKET)   // tipo de comparação - PACKET, STATUS, ou CONST 
//                    .setLhs(11)              // byte - bateria
//                    .setOperator(SDN_WISE_LESS)        // operador = < > = !=
//                    .setRhsLocation(SDN_WISE_CONST)    // tipo de comparação - PACKET, STATUS, ou CONST 
//                    .setRhs(230));                     // byte ou valor
//           entry.addAction(new ForwardUnicastAction().setNextHop(new NodeAddress(7)));
//        
//        ctrl.addRule((byte) 1, new NodeAddress(moteId), entry);

 */
