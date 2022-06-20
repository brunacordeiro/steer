
package com.github.sdnwiselab.sdnwise.cooja;

import com.github.sdnwiselab.sdnwise.packet.*;
import java.nio.charset.Charset;
//import static com.github.sdnwiselab.sdnwise.cooja.Constants.*;



public class EventHandler{

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



  private String  typeDataRequested;
  private boolean eventAlert, eventPeriodic;
  private float   limitMAX, limitMIN;
  private int     cont, idDado, sendInterval, idMessage;
  private Mote mote;
  private Agente agt;
  private DataPacket packet;

  public EventHandler(Mote m, Agente agt, DataPacket pck){
    typeDataRequested = null;
    sendInterval = 0;
    limitMAX = 0;
    limitMIN = 0;
    idMessage = 0;
    eventAlert = false;
    eventPeriodic = false;
    cont = 1;
    mote = m;
    this.agt = agt;
    packet = pck;
  }

  public void executeEvent(){

    if(eventAlert) {
      if(cont == 1){ // tempo minimo de envio - 1s
        checkDataAlert();
        cont = 1;
      }else{
        cont ++;
      }
    }

    else if(eventPeriodic){
      if(cont == sendInterval){ // tempo minimo de envio - 1s
        sendDataEvent();
        cont = 1;
      }else{
        cont ++;
      }
    }
  }

  public void sendDataEvent(){   //periodico
    mote.log("Tipo de Dado: " +  typeDataRequested);
    String message;

    switch(typeDataRequested){
      case DATA_TEMP: idDado = 2; break;
      case DATA_UMID: idDado = 3; break;
      case DATA_TVOC: idDado = 4; break;
      case DATA_CO2:  idDado = 5; break;
      default:
        packet.setPayload(MESSAGE_ERROR_SEND_DATA.getBytes(Charset.forName("UTF-8")));
      break;
    }
    //enviar pacote
    if(idDado > 1 && idDado < 6){
      message = "PERIODICO" + ", " + agt.dataRequested(idDado) + ";" + getSendInterval() + ", " + getIdMessage();
      packet.setPayload(message.getBytes(Charset.forName("UTF-8")));
    }
    packet.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
    mote.runFlowMatch(packet);
  }

  public void checkDataAlert(){
    String valor[] = null;
    String valReturn;

    switch(typeDataRequested){
      case DATA_TEMP:
          idDado = 2;
          valReturn = agt.dataRequested(idDado);
          valor = valReturn.split(";");
          float temp = Float.parseFloat(valor[1]);

          if(temp > getLimitMAX() || temp < getLimitMIN()){   //somente envia se estiver fora do limite
            sendPacketCheckData(MESSAGE_NOTIFY_LIMIT_TEMP + " [" + agt.getTemperatura() + " Graus]");
          }
        break;
      case DATA_UMID:
          idDado = 3;
          valReturn = agt.dataRequested(idDado);
          valor = valReturn.split(";");
          float umid = Float.parseFloat(valor[1]);

          if(umid > getLimitMAX() || umid < getLimitMIN()){   //somente envia se estiver fora do limite
            sendPacketCheckData(MESSAGE_NOTIFY_LIMIT_UMID + " [" + agt.getUmidade() + "%]");
          }
        break;
      case DATA_TVOC:
          idDado = 4;
          valReturn = agt.dataRequested(idDado);
          valor = valReturn.split(";");
          float tvoc = Float.parseFloat(valor[1]);

          if(tvoc > getLimitMAX()){   //somente envia se estiver fora do limite
            sendPacketCheckData(MESSAGE_NOTIFY_LIMIT_TVOC + " [" + agt.getTVOC() + " ppb]");
          }
        break;
      case DATA_CO2:
          idDado = 2;
          valReturn = agt.dataRequested(idDado);
          valor = valReturn.split(";");
          float co2 = Float.parseFloat(valor[1]);

          if(co2 > getLimitMAX()){   //somente envia se estiver fora do limite
            sendPacketCheckData(MESSAGE_NOTIFY_LIMIT_CO2 + " [" + agt.getCO2() + " ppm]");
          }
        break;
      default:
        sendPacketCheckData(MESSAGE_ERROR_REGISTER_ALERT);
      break;
    }
  }

  public void sendPacketCheckData(String message){
    String msgRetorn =  NOTIFY + ", " + message + ", " + getIdMessage();
    packet.setPayload(msgRetorn.getBytes(Charset.forName("UTF-8")));
    packet.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
    mote.runFlowMatch(packet);
  }

  public Integer getIdMessage(){
    return idMessage;
  }
  public void setIdMessage(int idMsg){
    idMessage = idMsg;
  }

  public String getDataType(){
    return typeDataRequested;
  }
  public void setDataType(String dataType){
    typeDataRequested = dataType;
  }

  public int getSendInterval(){
    return sendInterval;
  }
  public void setSendInterval(int freq){
    sendInterval = freq;
  }

  public boolean getEventAlert(){
    return eventAlert;
  }
  public void setEventAlert(boolean alert){
    eventAlert = alert;
  }

  public boolean getEventPeriodic(){
    return eventPeriodic;
  }
  public void setEventPeriodic(boolean periodic){
    eventPeriodic = periodic;
  }

  public float getLimitMAX(){
    return limitMAX;
  }
  public float getLimitMIN(){
    return limitMIN;
  }
  public void setLimitMAX(float max){
    limitMAX = max;
  }
  public void setLimitMIN(float min){
    limitMIN = min;
  }
}
