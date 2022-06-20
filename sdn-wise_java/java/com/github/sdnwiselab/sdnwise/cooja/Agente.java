
package com.github.sdnwiselab.sdnwise.cooja;

//import static com.github.sdnwiselab.sdnwise.cooja.Constants.*;
import com.github.sdnwiselab.sdnwise.packet.*;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import com.github.sdnwiselab.sdnwise.cooja.Mote;
import com.github.sdnwiselab.sdnwise.cooja.EventHandler;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.*;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.LinkedList;
import java.util.List;


import org.contikios.cooja.motes.AbstractApplicationMote;
import org.contikios.cooja.*;



public class Agente {

  // application msg Error
  public final static String MESSAGE_ERROR_SEND_DATA      = "Fator solicitado NAO identificado!";
  public final static String MESSAGE_ERROR_REGISTER_EVENT = "Erro ao cadastrar Evento";
  public final static String MESSAGE_ERROR_REGISTER_ALERT = "Erro ao cadastrar Alerta";
  public final static String MESSAGE_ERROR_DELETE_EVENT   = "Erro ao deletar Evento";
  public final static String MESSAGE_ERROR_CONSULT_EVENT  = "Erro ao consultar Evento";
  public final static String MESSAGE_ERROR_RECEIVE        = "Erro ao receber solicitacao do controlador";
  public final static String MESSAGE_ERROR_INTERACTION    = "Erro ao identificar o tipo de interação";
  public final static String MESSAGE_ERROR_RETURN_DATA    = "Erro ao retornar dado solicitado";

  //application msg notify
  public final static String MESSAGE_NOTIFY_REGISTER_EVENT = "Evento Cadastrado com Sucesso!";
  public final static String MESSAGE_NOTIFY_REGISTER_ALERT = "Alerta Cadastrado com Sucesso!";
  public final static String MESSAGE_NOTIFY_DELETE_EVENT   = "Evento Deletado!";
  public final static String MESSAGE_NOTIFY_LIMIT_TEMP     = "Alerta! Temperatura fora do limite recomendado!";
  public final static String MESSAGE_NOTIFY_LIMIT_UMID     = "Alerta! Umidade fora do limite recomendado!";
  public final static String MESSAGE_NOTIFY_LIMIT_TVOC     = "Alerta! TVOC fora do limite recomendado!";
  public final static String MESSAGE_NOTIFY_LIMIT_CO2      = "Alerta! CO2 fora do limite recomendado!";
  public final static String MESSAGE_NOTIFY_CONSULT_EVENT  = "Não ha eventos cadastrados para esse Mote!";

  public final static String ERROR  = "Erro";
  public final static String NOTIFY = "Notificacao";

  // application type sensors
  public final static String DATA_QUALIDADE = "Qualidade do Ar";
  public final static String DATA_TEMP      = "Temperatura";
  public final static String DATA_UMID      = "Umidade";
  public final static String DATA_TVOC      = "TVOC";
  public final static String DATA_CO2       = "CO2";




//declaração dos atributos da classe Agente

  public static String hour;
  public static float temperatura, umidade, tvoc, co2;
//  public final Date dataColeta = new Date();
//  public EventHandler event;

  public void gerarDadosSensores(){
      Random rand = new Random();

      temperatura = rand.nextInt(17) + 16;
      umidade = rand.nextInt(52) + 28;
      tvoc = rand.nextInt(600) + 1;
      co2 = rand.nextInt(1100) + 400;

      setTemperatura(temperatura);
      setUmidade(umidade);
      setTVOC(tvoc);
      setCO2(co2);
  }

  /* Recebe a solicitacao do controlador.
     *
     * @param packet contem o DataPacket enviado pelo controlador,
     *        o payload do pacote e composto por: tipo de dados solicitado, tipo de envio
     * @param mote contem uma instancia da classe Mote
     * @throws java.lang.Exception
     */

  public void receiveController(DataPacket packet, Mote mote){
      // requestController = "tipo de dado", "como deve ser enviado"
      String requestController = new String(packet.getPayload(),Charset.forName("UTF-8"));
      checkRequest(packet, requestController, mote);  //tenho uma string com os dados a serem enviados
      packet.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
      mote.runFlowMatch(packet);
  }

  public String checkRequest(DataPacket packet, String solicitation, Mote mote){   //verificar solicitação

    //event = new EventHandler(mote, this, packet);
    String arraySolicitation[] = solicitation.split(", ");

    String typeDataRequested = arraySolicitation[0];      //qualidade do ar, temperatura...
    String typeInteraction = arraySolicitation[1];           // polling, evento_periodico, evento_alerta
    int    sendInterval = Integer.parseInt(arraySolicitation[2]);  //intervalo de envio das msgs
    float limitAlertMAX = Float.parseFloat(arraySolicitation[3]);
    float limitAlertMIN = Float.parseFloat(arraySolicitation[4]);
    int    idMessage = Integer.parseInt(arraySolicitation[5]);

    String message, dadosEvento;
    mote.log("\n" + "Controlador solicita: " + typeDataRequested +
            " Tipo de Interacao: " + typeInteraction + " Intervalo de envio: "
            + sendInterval + "ms\n" + " Mote: " + mote);

    try{
      switch (typeInteraction) {
        case "polling":
          message = "POLLING" + ", " + dataRequested(1) + ", " + idMessage;
          packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
          break;

        case "periodic":

            if(mote.getEvent() == null){  //função dentro do AbstractMote
              EventHandler event = new EventHandler(mote, this, packet);
              event.setDataType(typeDataRequested);
              event.setSendInterval(sendInterval);
              event.setEventAlert(false);
              event.setEventPeriodic(true);
              event.setIdMessage(idMessage);

              mote.setEvent(event);
              dadosEvento = typeDataRequested +";"+ typeInteraction +";"+ sendInterval +";"+ limitAlertMAX +";"+ limitAlertMIN;
              message = NOTIFY + ", " + MESSAGE_NOTIFY_REGISTER_EVENT + ", " + idMessage;
              packet.setPayload(message.getBytes(Charset.forName("UTF-8")));

            }else {
              message = ERROR + ", " + MESSAGE_ERROR_REGISTER_EVENT + ", " + idMessage;
              packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
            }
          break;

        case "alert":
            if(mote.getEvent() == null){  //função dentro do AbstractMote
              EventHandler event = new EventHandler(mote, this, packet);
              event.setEventAlert(true);
              //event.setEventPeriodic(false);
              event.setDataType(typeDataRequested);
              event.setLimitMAX(limitAlertMAX);
              event.setLimitMIN(limitAlertMIN);
              event.setIdMessage(idMessage);
              mote.setEvent(event);
              dadosEvento = typeDataRequested +";"+ typeInteraction +";"+ sendInterval +";"+ limitAlertMAX +";"+ limitAlertMIN;
              message = NOTIFY + ", " + MESSAGE_NOTIFY_REGISTER_ALERT + ", " + idMessage;
              packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
            }
          break;
        case "deleteEvent":
          if(mote.getEvent() != null){

            mote.setEvent(null);
            message = NOTIFY  + ", " + MESSAGE_NOTIFY_DELETE_EVENT  + ", " + idMessage;
            packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
          }
          else{
            message = ERROR  + ", " + MESSAGE_ERROR_DELETE_EVENT  + ", " + idMessage;
            packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
          }
          break;


        default:
            String msg = ERROR  + ", " + MESSAGE_ERROR_INTERACTION  + ", " + idMessage;
            packet.setPayload(msg.getBytes(Charset.forName("UTF-8")));
          break;
      }
    }catch (Exception e) {
         String msg = "Exception"  + ", " + MESSAGE_ERROR_RECEIVE  + ", " + idMessage;
         packet.setPayload(msg.getBytes(Charset.forName("UTF-8")));
     }
    return null;

 } // fim do método de verificar solicitação do controlador


 public void registerConsult(int idMsg, String dados){ // modificar esse metodo

  String[] array = dados.split(";");
  String typeData  = array[0];
  String typeIntrc = array[1];
  int idDado = 0;
  int idIntrc = 0;

  switch(typeData){
    case DATA_TEMP: idDado = 2; break;
    case DATA_UMID: idDado = 3; break;
    case DATA_TVOC: idDado = 4; break;
    case DATA_CO2:  idDado = 5; break;
    default:
  }
  switch(typeIntrc){
    case "polling": idIntrc = 2; break;
    case "event":   idIntrc = 3; break;
    case "alert":   idIntrc = 4; break;
    default:
  }
  //typeDataRequested +";"+ typeInteraction +";"+ sendInterval +";"+ limitAlertMAX +";"+ limitAlertMIN;
  dados  =   idDado+";"+idIntrc+";"+array[2]+";"+array[3]+";"+array[4];
  //consult.put(idMsg, dados);
 }


  public String dataRequested(int idDado){
   gerarDadosSensores();
   SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm:ss");
   GregorianCalendar gc = new GregorianCalendar();
   hour = sdf.format(gc.getTime());

        try{
           switch(idDado){
             case 1:
               return idDado + ";" + temperatura + ";" + umidade + ";" +
                      tvoc + ";" + co2  + ";" + hour;
             case 2:
               return idDado + ";" + temperatura + ";" + hour;
             case 3:
               return idDado + ";" + umidade + ";" + hour;
             case 4:
               return idDado + ";" +  tvoc + ";" + hour;
             case 5:
               return idDado + ";" +  co2 + ";" + hour;
             default:
               break;
           }
         }
         catch (Exception e) {
           String messageError = "Erro ao retornar dado solicitado";
         }
    return null;
  }


  public Float getTemperatura(){
    return temperatura;
  }
  public void setTemperatura(Float temp){
    temperatura = temp;
  }
  public Float getUmidade(){
    return umidade;
  }
  public void setUmidade(Float umid){
    umidade = umid;
  }
  public Float getTVOC(){
    return tvoc;
  }
  public void setTVOC(Float TVOC){
    tvoc = TVOC;
  }
  public Float getCO2(){
    return co2;
  }
  public void setCO2(Float CO2){
    co2 = CO2;
  }




} // fim da classe Agente


/*  comentários...

//  case "Qualidade do Ar":

    //while(true){
      //mote.log("Aqui");
      //sendDataSensors = enviarDadosControlador(1);   //tipoDado, leitura dos sensores, hour
  //    return enviarDadosControlador(1);
      //DataPacket packet2 = new DataPacket(enviarDadosControlador(1).getBytes(Charset.forName("UTF-8")));
      //packet2.setPayload(enviarDadosControlador(1).getBytes(Charset.forName("UTF-8")));
      //packet2.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
      //mote.runFlowMatch(packet2);   //método de constroe a regra da tabela de fluxo
      //Thread.sleep(800);

  //  }
    //break;


      public String enviarQualidadeAr(DataPacket pck, Mote mote) {        //executado no callback do Sink

          try {
              Thread.sleep(5000);  // 5 segundos e enviar
              String enviarQualidade = enviarDadosControlador(1);
              return enviarQualidade;
              //sendController(pck, enviarQualidade, mote);
          } catch (Exception ex) {
              mote.log("Error enviarQualidadeAr");
          }
        return null;
      }


      mote.getSimulation().scheduleEvent(
              new MoteTimeEvent(mote, 0) {
                  @Override
                  public void execute(long t) {
                      mote.log("execute");
                      DataPacket packet3 = new DataPacket((enviarDadosControlador(2, intervalo).getBytes(Charset.forName("UTF-8"))));
                      packet3.setPayload(enviarDadosControlador(2, intervalo).getBytes(Charset.forName("UTF-8"))); //futuro
                      packet3.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
                      mote.runFlowMatch(packet3);
                  }
              },
              mote.getSimulation().getSimulationTime() + (1100) * Simulation.MILLISECOND
      );

  new Thread(){
    public void run(){
      for(int i = 0; i < 10; i++){
        mote.log("execute");
        DataPacket packet3 = new DataPacket((enviarDadosControlador(2, intervalo).getBytes(Charset.forName("UTF-8"))));
        packet3.setPayload(enviarDadosControlador(2, intervalo).getBytes(Charset.forName("UTF-8"))); //futuro
        packet3.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
        mote.runFlowMatch(packet3);

        try{
          Thread.sleep(1000);
        }catch (Exception e) {
          mote.log("aqui");
        }
      }
    }
  }.start();


*****TRECHO CDG simulation.scheduleEvent
// packet.setPayload(enviarDadosControlador(2, intervalo).getBytes(Charset.forName("UTF-8")));

//long tem = simulation.getSimulationTime() + (1000 + delay) * Simulation.MILLISECOND;
//log("time : " + time);
//DataPacket packet = new DataPacket("VITORIA NA GUERRA".getBytes(Charset.forName("UTF-8")));
//packet.setPayload("VITORIA NA GUERRA".getBytes(Charset.forName("UTF-8"))); //futuro
//packet.setSrc(addr).setDst(getActualSinkAddress()).setTtl((byte) ttl_max);
//runFlowMatch(packet);


if(typeInteraction.equals("polling")){   //tipo interacao == polling(checar)
  switch(typeDataRequested){
      case "Temperatura":
        packet.setPayload(dataRequested(2).getBytes(Charset.forName("UTF-8")));
        break;
      case "Umidade":
        packet.setPayload(dataRequested(3).getBytes(Charset.forName("UTF-8")));
        break;
      case "TVOC":
        packet.setPayload(dataRequested(4).getBytes(Charset.forName("UTF-8")));
        break;
      case "CO2":
        packet.setPayload(dataRequested(5).getBytes(Charset.forName("UTF-8")));
        break;
      case "controleTemperatura":
        packet.setPayload(controllData(12).getBytes(Charset.forName("UTF-8")));
        break;
      case "controleUmidade":
        packet.setPayload(controllData(13).getBytes(Charset.forName("UTF-8")));
        break;
      case "controleTVOC":
        packet.setPayload(controllData(14).getBytes(Charset.forName("UTF-8")));
        break;
      case "controleCO2":
        packet.setPayload(controllData(15).getBytes(Charset.forName("UTF-8")));
        break;
      default:
          packet.setPayload(dataRequested(1).getBytes(Charset.forName("UTF-8")));
      break;
    }
 } else if(typeInteraction.equals("evento")){ //tipo de interacao == evento_(tempo)

     switch(typeDataRequested){   //tipo do evento, periodicidade

         case "Temperatura":
            //verificar se existe um evento


           break;
       }

 } else if (typeInteraction.equals("alerta")){  //nome metrica, valor de limite

 }

 public String controllData(int idDado){
     gerarDadosSensores();

     switch(idDado){
       case 12:
         return idDado + " , " + (temperatura - (temperatura*0.1));
       case 13:
         return idDado + " , " + (umidade - (umidade*0.1));
       case 14:
         return idDado + " , " +  (tvoc - (tvoc*0.2));
       case 15:
         return idDado + " , " +  (co2 - (co2*0.2));
       default:
         return "Entrada Invalida!";
     }
 }


 /*
    JSONObject objMsgRequest = new JSONObject(solicitation);
    String  typeDataRequested = objMsgRequest.getString("typeSensor");
    String  typeInteraction     = objMsgRequest.getString("typeInteraction");
    Integer sendInterval        = objMsgRequest.getInt("Interval_Limit");
    Integer idMessage           = objMsgRequest.getInt("idMsg");
 */
