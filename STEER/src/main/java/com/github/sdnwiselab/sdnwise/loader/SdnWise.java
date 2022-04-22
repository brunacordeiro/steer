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
package com.github.sdnwiselab.sdnwise.loader;

import com.github.sdn_idn_iot.idn.Behavior;
import com.github.sdn_idn_iot.idn.BehaviorLogic;
import com.github.sdn_idn_iot.idn.Communication;
import com.github.sdn_idn_iot.idn.Intent;
import com.github.sdn_idn_iot.idn.Mediador;
import com.github.sdnwiselab.sdnwise.application.MessageHandler;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.controller.ControllerFactory;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SdnWise class of the SDN-WISE project. It loads the configuration file and
 * starts the the Controller.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class SdnWise {
    public int auxPeriodico = 0;
    public int auxPolling = 0;
    public int auxAlerta = 0;
    public int count = 0;
    /**
     * Starts the components of the SDN-WISE Controller.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        new SdnWise().startExample();
    }

    private Controller controller;

    /**
     * Starts the Controller layer of the SDN-WISE network. The path to the
     * configurations are specified in the configFilePath String. The options to
     * be specified in this file are: a "lower" Adapter, in order to communicate
     * with the flowVisor (See the Adapter javadoc for more info), an
     * "algorithm" for calculating the shortest path in the network. The only
     * supported at the moment is "DIJKSTRA". A "map" which contains
     * informations regarding the "TIMEOUT" in order to remove a non responding
     * node from the topology, a "RSSI_RESOLUTION" value that triggers an event
     * when a link rssi value changes more than the set threshold.
     *
     * @param configFilePath a String that specifies the path to the
     * configuration file.
     * @return the Controller layer of the current SDN-WISE network.
     */
    public Controller startController(String configFilePath) {
        InputStream configFileURI = null;
        if (configFilePath == null || configFilePath.isEmpty()) {
            configFileURI = this.getClass().getResourceAsStream("/config.ini");
        } else {
            try {
                configFileURI = new FileInputStream(configFilePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Configurator conf = Configurator.load(configFileURI);
        controller = new ControllerFactory().getController(conf.getController());
        new Thread(controller).start();
        return controller;
    }

    public void startExample() {
        controller = startController("");

        System.out.println("SDN-WISE Controller running....");

        // We wait for the network to start 
        try {
            Thread.sleep(60000);

            // Then we query the nodes
            //  while (true){    
            for (int i = 1; i < 7; i++) {
                int netId = 1;
                NodeAddress dst = new NodeAddress(i);
                NodeAddress src = new NodeAddress(1);

                DataPacket p = new DataPacket(netId, src, dst);
                p.setNxhop(src);
                String solicitacao = "" + ", " + "ping" + ", " + 0 + ", " + 0 + ", " + 0 + ", " + 1;
                p.setPayload(solicitacao.getBytes(Charset.forName("UTF-8")));
                controller.sendNetworkPacket(p);
                Thread.sleep(2000);  //msg de ping a cada 10s
            }
            // }

        } catch (InterruptedException ex) {
            Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\n ----- Inicializando o Mediador ----- \n");

        // parte do usuário do intent (Aplicação) - Recebe intent
        Intent intent = new Intent();
        intent.setNameIntent("Intent01"); //definir o nome da intencao
        intent.setMetric("Numero de mensagens");
        intent.setPeriodicity(5);
        //intent.setSensor("Temperatura");
        intent.setSensor("TVOC");
        int idMotes[] = new int[7];
        idMotes[0] = 1;
        idMotes[1] = 2;
        idMotes[2] = 3;
        idMotes[3] = 4;
        idMotes[4] = 5;
        idMotes[5] = 6;
//        idMotes[6] = 7;
//        idMotes[7] = 8;
//        idMotes[8] = 9;
//        idMotes[9] = 10;
//        idMotes[10] = 11;

        intent.setIdMotes(idMotes);//lista de sensores
        // fim da parte do intent (Aplicação)

        System.out.println("Intencao construida..." );

        HashMap<String, Behavior[]> intentMap = new HashMap<>();
        Behavior behavior[] = new Behavior[3];

       // System.out.println("Inicio do behavior[0]"); // PERIODICO

        behavior[0] = new Behavior(new BehaviorLogic() {

            @Override
            public void reify() {  //so vai ser executado quando o mediador.processIntents for chamado - classe anomymous 
                this.setFlag(true);

                for (int mote = 1; mote < intent.getIdMotes().length; mote++) {
                   // auxTX = auxTX + 1;  // contar msg transmitidas
                    this.getInteraction().eventoPeriodico(intent.getSensor(), 
                            intent.getIdMotes()[mote], intent.getPeriodicity(), 
                            new MessageHandler() {
                        @Override
                        public void message(String dado, int moteId) {
                            auxPeriodico = auxPeriodico + 1;   //contar msg recebidas
                            //dado =  1;15.0;12.0;200.0;1819.0;10:34:25
                            //
                          // System.out.println("Primeiro Comportamento: " + dado + " - " + auxPeriodico);
                        }
                    }); // fim interaction.polling
                    esperaEnvioMensagem();
                } // fim do for
                waitBehaviorExec();
              //  System.out.println("Fim do for do behavior[0]");
            } // fim reify

            @Override
            public void delete() {

                for (int mote = 1; mote < intent.getIdMotes().length; mote++) {
                   // auxTX = auxTX + 1;  // contar msg transmitidas
                    this.getInteraction().delete("deleteEvent", intent.getIdMotes()[mote], new MessageHandler() {

                        @Override
                        public void message(String dado, int moteId) {
                             //System.out.println(dado);
                        }
                    }); // fim interaction.delete
                    esperaEnvioMensagem();
                } // fim do for
            }
        }); // fim do behavior[0]

       // System.out.println("\n\nInicio do behavior[1]");  // POLLING
        //BEHAVIOR[1]
        behavior[1] = new Behavior(new BehaviorLogic() {
            @Override
            public void reify() {  //so vai ser executado quando o mediador.processIntents for chamado - classe anomymous 
                this.setFlag(true);
                while (this.getFlag()) {
                    for (int mote = 1; mote < intent.getIdMotes().length; mote++) {
                        this.getInteraction().polling("Qualidade do Ar", intent.getIdMotes()[mote], new MessageHandler() {
                            @Override
                            public void message(String dado, int moteId) {
                                //dado =  1;15.0;12.0;200.0;1819.0;10:34:25
                                //
                                auxPolling = auxPolling + 1;
                              // System.out.println("Segundo Comportamento: " + moteId + " - " + dado);
                            }
                        }); // fim interaction.polling
                        esperaEnvioMensagem();
                    } // fim do for
                    try {
                        Thread.sleep((intent.getPeriodicity() * 1000) - ((intent.getIdMotes().length - 1) * 100));
                    } catch (InterruptedException ex) {
                        this.setFlag(false);
                        //System.out.println("THREAD STOPADA POLLING!");
                    }
                }
           //     System.out.println("Fim do for do behavior[1]");
            } // fim reify

            @Override
            public void delete() {

            }
        }); // fim do behavior[1]

     //   System.out.println("Inicio do behavior[2]"); // ALERTA

        behavior[2] = new Behavior(new BehaviorLogic() {

            @Override
            public void reify() {  //so vai ser executado quando o mediador.processIntents for chamado - classe anomymous 
                this.setFlag(true);

                for (int mote = 1; mote < intent.getIdMotes().length; mote++) {

                    this.getInteraction().pollingAlerta("Qualidade do Ar", intent.getIdMotes()[mote], new MessageHandler() {
                        @Override
                        public void message(String dado, int moteId) {
                            String[] array = dado.split(";");
                            float dadoSensor = Float.parseFloat(array[3]);
                            // 1 = Temperatura; 2 = Umidade; 3 = TOVC; 04 = CO2
                            //System.out.println("\n" + intent.getSensor()+ ": " + dadoSensor + " Mote: " + moteId);
                            auxAlerta = auxAlerta + 1;
                           // System.out.println(auxAlerta + " Terceiro Comportamento - Coleta dados: " + dadoSensor + " Mote: " + moteId);
                            analiseAlerta(dadoSensor, moteId);
                        }
                    });                    
                    esperaEnvioMensagem();
                } // fim do for
                waitBehaviorExec();
            //    System.out.println("Fim do for do behavior[2] - ALERTA");
            } // fim reify

            @Override
            public void delete() {

                for (int mote = 1; mote < intent.getIdMotes().length; mote++) {

                    this.getInteraction().delete("deleteEvent", intent.getIdMotes()[mote], new MessageHandler() {

                        @Override
                        public void message(String dado, int moteId) {
                            //System.out.println("delete: " + dado);
                        }
                    }); // fim interaction.delete
                    esperaEnvioMensagem();
                } // fim do for
            }

            public void analiseAlerta(Float dadoSensor, int moteId) {
                
                String valoresAlerta = (dadoSensor + 1) + ", " + (dadoSensor - 1);
//                System.out.println("Analisa Alerta: "  + intent.getSensor()+ ": " + dadoSensor + " Limites: "
//                        + valoresAlerta + " Mote: " + moteId);

                this.getInteraction().alerta(intent.getSensor(), moteId, dadoSensor, valoresAlerta, new MessageHandler() {
                    @Override
                    public void message(String dado, int moteId) {
                        //dado =  1;15.0;12.0;200.0;1819.0;10:34:25      
                        count = count + 1;
                        //System.out.println("Terceiro Comportamento - Alerta: " + dado + " Mote: " + moteId + " - " + count);
                        if (dado.contains("Alerta Cadastrado com Sucesso!")) {
                        } else {
                            deleteAlerta(Float.parseFloat(dado), moteId);
                            //System.out.println("Alerta Deletado");
                        }
                    }
                }); // fim interaction.alerta
            }

            public void deleteAlerta(Float dadoAlerta, int moteId) {
               // System.out.println("Delete Alerta - DadoAlerta --------- " + dadoAlerta + "\n"); 
                this.getInteraction().delete("deleteEvent", moteId, new MessageHandler() {
                    @Override
                    public void message(String dado, int moteId) {
                        analiseAlerta(dadoAlerta, moteId);
                       // System.out.println("deleteAlerta: " + dado + " Mote: " + moteId + "\n");
                    }
                });
            }
        }); // fim do behavior[2]

        intentMap.put(intent.getNameIntent(), behavior);
     //   System.out.println("IntentMap construido");

       // System.out.println("Instanciando a communication");
        Communication interaction = new Communication(controller);
 //       interaction.getMonitoringMetrics().startMsg();
        
        System.out.println("Instanciando o Mediador...");
        Mediador mediador = new Mediador(intentMap, interaction);

        System.out.println("Executando o processIntents");
        mediador.processIntents(intent);
//        interaction.getMonitoringMetrics().stopMsg();
    } // fim do metodo startExemple

} // fim da classe SdnWise.java

