/*
 * Copyright (C) 2021 User
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
 * @author User
 */
public class ResultMetrics {
    
    private float[] results;
    private int[] resultMsg;
    
    public float[] getResults(){
        return results;
    }
    
    public void setResults(float[] result){
        this.results = result;
    }

    public int[] getResultsMsg(){
        return resultMsg;
    }    
    public void setResultsMsg(int[] resMsg){
        this.resultMsg = resMsg;
    }
    
}
