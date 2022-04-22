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
package com.github.sdnwiselab.sdnwise.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the three Map<String,String> containing the configuration
 * parameters for the lower adapter, the algorithm and the network map of a
 * controller object.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class ConfigController {

    private final Map<String, String> lower = new HashMap<>();
    private final Map<String, String> algorithm = new HashMap<>();
    private final Map<String, String> map = new HashMap<>();

    /**
     * Returns an unmodifiableMap containing the configurations for the network
     * map.
     *
     * @return a Map<String,String> containing the configurations for the
     * network map
     */
    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns an unmodifiableMap containing the configurations for the lower
     * Adapter.
     *
     * @return a Map<String,String> containing the configurations for the lower
     * Adapter
     * @see com.sdn.wise.adapter.Adapter
     */
    public Map<String, String> getLower() {
        return Collections.unmodifiableMap(lower);
    }

    /**
     * Returns an unmodifiableMap containing the configurations for the
     * algorithm used
     *
     * @return a Map<String,String> containing the configurations for the
     * algorithm used
     */
    public Map<String, String> getAlgorithm() {
        return Collections.unmodifiableMap(algorithm);
    }
}
