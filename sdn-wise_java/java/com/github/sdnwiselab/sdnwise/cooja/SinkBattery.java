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
package com.github.sdnwiselab.sdnwise.cooja;

/**
 * Simulates a Battery that never consumes energy.
 *
 * @author Sebastiano Milardo
 */
public class SinkBattery extends Battery {

    @Override
    public Battery transmitRadio(int nByte) {
        return this;
    }

    @Override
    public Battery receiveRadio(int nByte) {
        return this;
    }

    @Override
    public Battery keepAlive(int seconds) {
        return this;
    }
}
