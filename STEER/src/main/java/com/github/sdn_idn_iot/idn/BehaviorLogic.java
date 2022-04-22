/*
 * Copyright (C) 2021 miche
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
package com.github.sdn_idn_iot.idn;

/**
 *
 * @author miche
 */
public abstract class BehaviorLogic implements Runnable {


    private Communication interaction;
    private Intent intent;
    private boolean flag;


    public abstract void reify();

    public abstract void delete();

    @Override
    public void run() {
        reify();
    }

    public void waitBehaviorExec() {

        while (flag) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                flag = false;
                delete();
               // System.out.println("THREAD STOPADA!");

            }
        }
    }

    public void esperaEnvioMensagem() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            flag = false;
            delete();
          //  System.out.println("THREAD STOPADA!");
        }
    }
    
        public void defineTime(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException ex) {
            flag = false;
            delete();
          //  System.out.println("THREAD STOPADA!");
        }
    }


    /**
     * @return the interaction
     */
    public Communication getInteraction() {
        return interaction;
    }

    /**
     * @param interaction the interaction to set
     */
    public void setInteraction(Communication interaction) {
        this.interaction = interaction;
    }

    /**
     * @return the intent
     */
    public Intent getIntent() {
        return intent;
    }

    /**
     * @param intent the intent to set
     */
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    /**
     * @return the intent
     */
    public boolean getFlag() {
        return flag;
    }

    /**
     * @param intent the intent to set
     */
    public void setFlag(boolean flag) {
        this.flag = flag;
    }



}
