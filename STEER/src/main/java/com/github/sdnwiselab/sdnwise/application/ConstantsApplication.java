/*
 * Copyright (C) 2021 bruna
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
package com.github.sdnwiselab.sdnwise.application;

/**
 *
 * @author bruna
 */
public class ConstantsApplication {

    // analise dos fatores
    public static String CRITICO = "Critico!";
    public static String ALARMANTE = "Alarmante!";
    public static String REGULAR = "Regular!";
    public static String BOM = "Bom!";
    public static String DENTRO_LIMITE = "Valor dentro do limite estabelecido em Legislacao";

    //SOLICITAR DADOS AO CONTROLADOR
    public static String QUALIDADE_AR = "Qualidade do Ar";
    public static String TEMPERATURA = "Temperatura";
    public static String UMIDADE = "Umidade";
    public static String TVOC = "TVOC";
    public static String CO2 = "CO2";

    public static String TEMP_ELEVADA = "Temperatura ACIMA da faixa permitida!";
    public static String TEMP_ABAIXO = "Temperatura ABAIXO da faixa permitida!";
    public static String TEMP_ADEQUADA = "Temperatura Adequada!";
    public static String UMID_ELEVADA = "Umidade ACIMA da faixa permitida!";
    public static String UMID_ABAIXO = "Umidade ABAIXO da faixa permitida!";
    public static String UMID_ADEQUADA = "Umidade Adequada!";

    public static final float MAX_TEMP = 28;
    public static final float MIN_TEMP = 20;

    public static final float MAX_UMID = 65;
    public static final float MIN_UMID = 35;

    public static final float MAX_TVOC = 500;        //valor mínimo não é relevante

    public static final float MAX_CO2 = 1000;        //valor mínimo não é relevante

    // application msg Error
    public final static String MESSAGE_ERROR_SEND_DATA = "Fator solicitado NAO identificado!";
    public final static String MESSAGE_ERROR_REGISTER_EVENT = "Erro ao cadastrar Evento";
    public final static String MESSAGE_ERROR_REGISTER_ALERT = "Erro ao cadastrar Alerta";
    public final static String MESSAGE_ERROR_DELETE_EVENT = "Erro ao deletar Evento";
    public final static String MESSAGE_ERROR_CONSULT_EVENT = "Erro ao consultar Evento";
    public final static String MESSAGE_ERROR_RECEIVE = "Erro ao receber solicitacao do controlador";
    public final static String MESSAGE_ERROR_INTERACTION = "Erro ao identificar o tipo de interação";
    public final static String MESSAGE_ERROR_RETURN_DATA = "Erro ao retornar dado solicitado";

    //application msg notify
    public final static String MESSAGE_NOTIFY_REGISTER_EVENT = "Evento Cadastrado com Sucesso!";
    public final static String MESSAGE_NOTIFY_REGISTER_ALERT = "Alerta Cadastrado com Sucesso!";
    public final static String MESSAGE_NOTIFY_DELETE_EVENT = "Evento Deletado!";
    public final static String MESSAGE_NOTIFY_LIMIT_TEMP = "Alerta! Temperatura fora do limite recomendado!";
    public final static String MESSAGE_NOTIFY_LIMIT_UMID = "Alerta! Umidade fora do limite recomendado!";
    public final static String MESSAGE_NOTIFY_LIMIT_TVOC = "Alerta! TVOC fora do limite recomendado!";
    public final static String MESSAGE_NOTIFY_LIMIT_CO2 = "Alerta! CO2 fora do limite recomendado!";
    public final static String MESSAGE_NOTIFY_CONSULT_EVENT = "Não ha eventos cadastrados para esse Mote!";

    public final static String ERROR = "Erro";
    public final static String NOTIFY = "Notificacao";

    // application type sensors
    public final static String DATA_QUALIDADE = "Qualidade do Ar";
    public final static String DATA_TEMP = "Temperatura";
    public final static String DATA_UMID = "Umidade";
    public final static String DATA_TVOC = "TVOC";
    public final static String DATA_CO2 = "CO2";

    private ConstantsApplication() {

    }
}
