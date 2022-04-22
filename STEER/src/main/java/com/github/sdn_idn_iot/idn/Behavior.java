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
public class Behavior {

    BehaviorLogic behaviorLogic;
    Thread t;

    public Behavior(BehaviorLogic bl) {
        behaviorLogic = bl;
    }

    public void execute(Communication interaction, Intent intent) {
        
        t = new Thread(behaviorLogic);        
        behaviorLogic.setInteraction(interaction);
        behaviorLogic.setIntent(intent);
        t.start();
    }

    public void stop() {
        t.interrupt();
    }

}
