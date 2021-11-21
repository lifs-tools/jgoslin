/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.domain;

import java.util.Map;
import java.util.TreeMap;

/**
 * An abstraction for double bond counts and positions.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class DoubleBonds {

    private int numDoubleBonds;
    protected final Map<Integer, String> doubleBondPositions;

    public DoubleBonds() {
        numDoubleBonds = 0;
        doubleBondPositions = new TreeMap<>();
    }

    public DoubleBonds(int num) {
        numDoubleBonds = num;
        doubleBondPositions = new TreeMap<>();
    }

    public DoubleBonds(TreeMap<Integer, String> db) {
        numDoubleBonds = db.size();
        doubleBondPositions = db;
    }

    public DoubleBonds copy() {
        DoubleBonds db = new DoubleBonds(numDoubleBonds);
        doubleBondPositions.entrySet().forEach(kv -> {
            db.doubleBondPositions.put(kv.getKey(), kv.getValue());
        });
        return db;
    }

    public int getNumDoubleBonds() {
        if (doubleBondPositions.size() > 0 && doubleBondPositions.size() != numDoubleBonds) {
            throw new ConstraintViolationException("Number of double bonds '" + Integer.toString(numDoubleBonds) + "' does not match to number of double bond positions '" + Integer.toString(doubleBondPositions.size()) + "'");
        }
        return numDoubleBonds;
    }

    public void setNumDoubleBonds(int numDoubleBonds) {
        this.numDoubleBonds = numDoubleBonds;
    }

    public Map<Integer, String> getDoubleBondPositions() {
        return doubleBondPositions;
    }

}
