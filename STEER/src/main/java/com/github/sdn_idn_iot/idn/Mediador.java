package com.github.sdn_idn_iot.idn;

import com.github.sdnwiselab.sdnwise.application.MonitoringMetrics;
import java.util.HashMap;

public class Mediador {

    private HashMap<String, Behavior[]> intentMap;
    private Communication interaction;
    private MonitoringMetrics monitoringMetrics;
    private ResultMetrics[] result;
    private ResultMetrics[] resultMsg;
    //private int[] resMsg = new int[3];
    private int aux;
    private int auxTX = 0;
    private String res = null;
    public int res0, res1, res2;

    public Mediador(HashMap<String, Behavior[]> intentMap, Communication interaction) {
        this.intentMap = intentMap;
        this.interaction = interaction;
        this.result = null;  //quantidade de comportamentos
        this.resultMsg = null;
        this.monitoringMetrics = monitoringMetrics;
    }

    public void processIntents(Intent intent) {        
        //executeSingle(intent, 0, 3600000); // executar um comportamento de cada vez
        //executeSingle(intent, 1, 9); // executar um comportamento de cada vez
        executeMediador(intent, 28000);  //executar o mediador e definir a janela de observação
    }

    public void esperar(int temp) {
        try {
            Thread.sleep(temp);
        } catch (InterruptedException ex) {
            System.err.println("Error");
        }
    }

    public void print(ResultMetrics res, float j) {
        System.out.print("Resultado " + j + " [");
        for (int i = 0; i < res.getResults().length; i++) {
            System.out.print(" " + res.getResults()[i] + " ");
        }
        System.out.println("] ");
    }
    
    /*
    * recebe a intenção, o comportamento a ser executado manualmente
    * e o tempo de execução de cada comportamento
    */    
    public void executeSingle(Intent intent, int bhv, int time){
        System.out.println("Executando behavior: " + bhv 
                + " por " + (time +1) + " janelas de observação...");
        Behavior behavior[] = (Behavior[]) intentMap.get(intent.getNameIntent());
        result = new ResultMetrics[behavior.length];
        resultMsg = new ResultMetrics[behavior.length];
        System.out.println("\nExecutando Observação: " + 1);
        interaction.getMonitoringMetrics().start();  // bateria
        interaction.getMonitoringMetrics().startMsg();   // msg
        behavior[bhv].execute(interaction, intent);       
        esperar(30000);  //waitBehaviorExec - tempo de observacao do comportamento 3 minutos        
        interaction.getMonitoringMetrics().stop(); //bateria
        interaction.getMonitoringMetrics().stopMsg();
        
        for(int i = 0; i < time;i ++){
            System.out.println("\nExecutando Observação: " + (i + 2));
            interaction.getMonitoringMetrics().start();  // bateria
            interaction.getMonitoringMetrics().startMsg();   // msg
            esperar(30000);  //janela de observação       
            interaction.getMonitoringMetrics().stop(); //bateria
            interaction.getMonitoringMetrics().stopMsg();
        }
        behavior[bhv].stop();
        esperar(2000);
        interaction.getMonitoringMetrics().stopMsg();
        
//        System.out.println("Qnt Msg: " + monitoringMetrics.getQntMsg()[0]);
    }
    
    /*
    * recebe a intenção e o tempo da janela de observação
    *
    */ 
    
    public void executeMediador(Intent intent, int time){
        Behavior behavior[] = (Behavior[]) intentMap.get(intent.getNameIntent());
        result = new ResultMetrics[behavior.length];
        resultMsg = new ResultMetrics[behavior.length];
        //monitoringMetrics = new MonitoringMetrics();
        
        for (int i = 0; i < behavior.length; i++) {

            System.out.println("\nInicio behavior " + i);
            interaction.getMonitoringMetrics().start();
            interaction.getMonitoringMetrics().startMsg();
            behavior[i].execute(interaction, intent);  // 0, 1 , 2
            esperar(time);  //waitBehaviorExec - janela de observação
            interaction.getMonitoringMetrics().stop();
            behavior[i].stop();

            result[i] = new ResultMetrics();
            result[i].setResults(interaction.getMonitoringMetrics().getResultMetrics().clone());
            print(result[i], i);
            esperar(2000);
            
            resultMsg[i] = new ResultMetrics();
            resultMsg[i].setResultsMsg(interaction.getMonitoringMetrics().getQntMsg());
            interaction.getMonitoringMetrics().stopMsg();
           // System.out.println("Msg:" + resultMsg);
            
            System.out.println("Fim behavior " + i + "\n");
        }
        
        // VERIFICAR MENOR CONSUMO DE BATERIA 
        float decision[] = new float[behavior.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].getResults().length; j++) {
                decision[i] += result[i].getResults()[j];
            }
        }
        int optBehavior = -1;
        float minMetrics = Float.MAX_VALUE;

        for (int i = 0; i < decision.length; i++) {
            if (decision[i] < minMetrics) {
                optBehavior = i;
                minMetrics = decision[i];
            }
        }

        // VERIFICAR MENOS QUANTIDADE DE MSG
        int decisionMsg[] = new int[behavior.length];
        for (int j = 0; j < 3; j++) {
            decisionMsg[j] = resultMsg[j].getResultsMsg()[j];
        }
      
        int optBehaviorMsg = -1;
        int minMetricsMsg = Integer.MAX_VALUE;

        for (int i = 0; i < decisionMsg.length; i++) {
            if (decisionMsg[i] < minMetricsMsg) {
                optBehaviorMsg = i;
                minMetricsMsg = decisionMsg[i];
            }
        }
    switch(optBehaviorMsg){
        case 0: 
            res = "Primeiro Comportamento";
            break;
        case 1: 
            res = "Segundo Comportamento";
            break;
        case 2: 
            res = "Terceiro Comportamento";
            break;
    }

    System.out.println("****************************************************"
            + "\nMelhor Comportamento Bateria: " + optBehavior 
            + "\nMelhor Comportamento Msg: " + res + " Qnt Msg: " + minMetricsMsg
            +  "\n****************************************************");
    
    //        behavior[optBehavior].execute(interaction, intent);
    //   behavior[optBehaviorMsg].execute(interaction, intent);
        executeSingle(intent, optBehaviorMsg, 6); // executando por 5 minutos
    
    }

}// fim do Mediador()

/* COMENTÁRIOS


        //behavior[0].execute(interaction, intent);
        //behavior[0].execute(interaction, intent);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            System.err.println("Error");
//        }


//        String mote = netPacket.getSrc().toString();
//        int qntMotes = intent.getIdMotes().length;
//        
//        for(int i = 0; i < qntMotes; i++){
//            System.out.println("ReportPacket do mote: " + mote);
//        }
//        
//        String ttl = String.valueOf(netPacket.getTtl());
//        String bateria = String.valueOf(rp.getBatt());
//        String vizinhos = rp.getNeighborsHashMap().toString();
//        String vzn = vizinhos.replaceAll("=-1", "");
//
//        String dadosColetados = mote + "," + ttl + "," + bateria;


 */
