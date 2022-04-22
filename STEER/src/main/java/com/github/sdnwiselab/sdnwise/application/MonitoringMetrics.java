package com.github.sdnwiselab.sdnwise.application;

import com.github.sdn_idn_iot.idn.Behavior;
import com.github.sdn_idn_iot.idn.Communication;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;

public class MonitoringMetrics {

    private int idMote = 0;
    private int mote = 0;
    private int aux = 0;
    private float battery = 0;
    private int flag = 0;
    private int flagMsg = 0;
    private int countMsgTX = 0;    
    private int countMsgRX = 0;
    public boolean count = false;
    public int resultMsg = 0;
    private float[] startMetrics;
    private float[] stopMetrics;
    private float[] resultMetrics;
    private int[] qntMsg;
    public String qntMsgAux;
    public Communication communication;
    public Behavior behavior;

    public MonitoringMetrics() {
        startMetrics = new float[6];        //qnt de motes na rede
        stopMetrics = new float[6];
        resultMetrics = new float[6];
        qntMsg = new int[3];
        qntMsgAux = null;
        countMsgTX = 0;
        countMsgRX = 0;
    }

    public void receiveMetrics(NetworkPacket netPacket) {
        ReportPacket rp = new ReportPacket(netPacket);

        String mote = netPacket.getSrc().toString();
        String m = mote.replaceAll("0.", "");
        idMote = Integer.parseInt(m);
        setMote(idMote);
        String bateria = String.valueOf(rp.getBatt());
        setBattery(Integer.parseInt(bateria));
        collectMetrics(getMote(), getBattery());
        //collectMessage(getCountMsgTX(), getCountMsgRX());
    }

    public void start() {
        for (int i = 0; i < 6; i++) {
            startMetrics[i] = 0;
            stopMetrics[i] = 0;
            resultMetrics[i] = 0;
        }
        flag = 1;   //apto para começar a medir
    }


    public void stop() { // battery
        flag = 2;
        for (int i = 0; i < 6; i++) {
            resultMetrics[i] = startMetrics[i] - stopMetrics[i];   // metrica - bateria
        }
//        print(startMetrics, "Stop - Start");
//        print(stopMetrics, "Stop - Stop");
//        print(resultMetrics, "Stop - Result");
    }
    
        
    public void startMsg() {
        countMsgTX = 0;
        countMsgRX = 0;
        flagMsg = 1;   //apto para começar a medir as msg
       // System.out.println("startMSG TX: " + getCountMsgTX() + " RX: " + getCountMsgRX());
        setCount(false);
    }
    
    public void stopMsg() { 
        flagMsg = 2;
        setCount(false); // condição de parada para a classe Communication
        setResultMsg(getCountMsgTX() +  getCountMsgRX());
        //System.out.println("Qnt Msg: " + getResultMsg());
        System.out.println("stoptMSG TX: " + getCountMsgTX() + " RX: " + getCountMsgRX() 
                + " result: " + getResultMsg());
       
        if(aux < 3 && qntMsg[aux] == 0 ){
            qntMsg[aux] = getResultMsg();
            aux = aux + 1;
        }        
        //setQntMsg(qntMsg);
        qntMsg();
    }

    public void qntMsg(){        
        qntMsgAux = qntMsg[0] + ", "+ qntMsg[1] +", "+ qntMsg[2];
      //  System.out.println("qntMsg Comportamentos: " + qntMsgAux);
      //  return qntMsgAux;
   }

    public void collectMetrics(int mote, float batt) {

        if (flag == 1) {
            if (startMetrics[mote - 1] == 0) {
                //startMetrics[mote - 1] = (batt/255)*5000;
                startMetrics[mote - 1] = batt;
            }
            //stopMetrics[mote - 1] = (batt/255)*5000;
            stopMetrics[mote - 1] = batt;
        } else if (flag == 2) {
            System.out.println("Metrics stop!");
            flag = 0;
        }
    }
    

    public void print(float[] res, String x) {
        System.out.print(x + ": [");
        for (int i = 0; i < res.length; i++) {
            System.out.print(" " + res[i] + " ");
        }
        System.out.println("]");
    }

    /**
     * @return the mote
     */
    public int getMote() {
        return mote;
    }

    /**
     * @param mote the mote to set
     */
    public void setMote(int mote) {
        this.mote = mote;
    }
    
    /**
     * @return the count Msg
     */
    public int getCountMsgTX() {
        return countMsgTX;
    }

    /**
     * @param count the count to msg
     */
    public void setCountMsgTX(int countMsgTX) {
        this.countMsgTX = countMsgTX;
    }
    
        
    /**
     * @return the count Msg
     */
    public int[] getQntMsg() {
        return qntMsg;
    }
//    
//    public void setQntMsg(int[] qntMsg){
//        this.qntMsg = qntMsg;
//    }
//    
        /**
     * @return the count Msg
     */
    public String getQntMsgAux() {
        return qntMsgAux;
    }

    /**
     * @param count the count to msg
     */
    public void setCountMsgRX(int countMsgRX) {
        this.countMsgRX = countMsgRX;
    }
    
    public int getCountMsgRX() {
        return countMsgRX;
    }
        /**
     * @return the count Msg
     */
    public boolean getCount() {
        return count;
    }

    /**
     * @param count the count to msg
     */
    public void setCount(boolean count) {
        this.count = count;
    }

    /**
     * @return the battery
     */
    public float getBattery() {
        return battery;
    }

    /**
     * @param battery the battery to set
     */
    public void setBattery(float battery) {
        this.battery = battery;
    }

    public float[] getResultMetrics() {
        return resultMetrics;
    }
    
    /**
     * @param battery the battery to set
     */
    public void setResultMsg(int resultMsg) {
        this.resultMsg = resultMsg;
    }

    public int getResultMsg() {
        return resultMsg;
    }

} // fim do MonitoringMetrics


/* Comentarios

        
        System.out.println("\n*****************************************************************");
        System.out.println("Informacoes do Mote: " + mote);
        System.out.println("TTL: " + ttl);
        System.out.println("Bateria: " + bateria);
        System.out.println("Vizinhos (Hash) : " + vizinhos);
        System.out.println("*****************************************************************\n");



 */
