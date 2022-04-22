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

import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.*;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author bruna
 */
public abstract class AbstractApplication implements Observer{

    public final Controller ctrl;
    public int countMsgData = 0;
    public int countMsgControl = 0;

    public AbstractApplication(Controller ctrl){
        this.ctrl = ctrl;
        this.ctrl.addObserver(this);
    }    
    
    public Controller getController(){
        return this.ctrl;
    }
    
    
    @Override
    public void update(Observable o, Object arg) {              //atualização do adaptador ou da representação da rede
        NetworkPacket data = (NetworkPacket) arg;
        
         switch (data.getType()) {
             case SDN_WISE_DATA:
                receivePacket(new DataPacket (data));
                break;
            case SDN_WISE_REPORT:
            case SDN_WISE_BEACON:
            case SDN_WISE_RESPONSE:
            case SDN_WISE_OPEN_PATH:
                receiveMetrics(data);
                break;
        }
    }
    
    public abstract void receivePacket(DataPacket data);
    
   // public abstract void sendPakcet(DataPacket data);
       
    public abstract void receiveMetrics(NetworkPacket data);
    
}




