/*
MIT License

Copyright (c) the authors (listed in global LICENSE file)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package org.lifstools.jgoslin.domain;

import java.util.HashMap;

/**
 *
 * @author dominik
 */
public class DoubleBonds {

    public int numDoubleBonds;
    public final HashMap<Integer, String> doubleBondPositions;

    public DoubleBonds() {
        numDoubleBonds = 0;
        doubleBondPositions = new HashMap<>();
    }

    public DoubleBonds(int num) {
        numDoubleBonds = num;
        doubleBondPositions = new HashMap<>();
    }

    public DoubleBonds copy() {
        DoubleBonds db = new DoubleBonds(numDoubleBonds);
        doubleBondPositions.entrySet().forEach(kv -> {
            db.doubleBondPositions.put(kv.getKey(), kv.getValue());
        });
        return db;
    }

    public int getNum() {
        if (doubleBondPositions.size() > 0 && doubleBondPositions.size() != numDoubleBonds) {
            throw new ConstraintViolationException("Number of double bonds '" + Integer.toString(numDoubleBonds) + "' does not match to number of double bond positions '" + Integer.toString(doubleBondPositions.size()) + "'");
        }
        return numDoubleBonds;
    }
}
