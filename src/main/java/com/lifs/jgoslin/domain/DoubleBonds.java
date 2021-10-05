/*
 * Copyright 2021 dominik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lifs.jgoslin.domain;

import java.util.HashMap;

/**
 *
 * @author dominik
 */
public class DoubleBonds{
    
    public int num_double_bonds;
    public HashMap<Integer, String> double_bond_positions;

    
    public DoubleBonds(){
        num_double_bonds = 0;
        double_bond_positions = new HashMap<>();
    }
    
    public DoubleBonds(int num) {
        num_double_bonds = num;
        double_bond_positions = new HashMap<>();
    }


    public DoubleBonds copy(){
        DoubleBonds db = new DoubleBonds(num_double_bonds);
        double_bond_positions.entrySet().forEach(kv -> {
            db.double_bond_positions.put(kv.getKey(), kv.getValue());
        });
        return db;
    }


    public int get_num() throws ConstraintViolationException {
        if (double_bond_positions.size() > 0 && double_bond_positions.size() != num_double_bonds)
        {
            throw new ConstraintViolationException("Number of double bonds '" + Integer.toString(num_double_bonds) + "' does not match to number of double bond positions '" + Integer.toString(double_bond_positions.size()) + "'");
        }
        return num_double_bonds;
    }
}
