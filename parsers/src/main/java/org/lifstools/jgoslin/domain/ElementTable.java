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

import java.util.EnumMap;

/**
 * Accounting table for chemical element frequency. This is used to calculate
 * sum formulas and total masses for a given chemical element distribution, e.g.
 * in a lipid.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class ElementTable extends EnumMap<Element, Integer> {

    private ElementTable(Class<Element> keyType) {
        super(keyType);
    }

    /**
     * Create a new element table.
     */
    public ElementTable() {
        this(Element.class);
        Elements.ELEMENT_MASSES.keySet().forEach(e -> {
            put(e, 0);
        });
    }

    /**
     * Create a new element table from the provided entries.
     * @param entries the entries
     * @return a new element table.
     */
    public static ElementTable of(Entry<Element, Integer>... entries) {
        ElementTable et = new ElementTable();
        for (Entry<Element, Integer> e : entries) {
            et.put(e.getKey(), e.getValue());
        }
        return et;
    }

    /**
     * Add all elements and counts to those in this element table.
     *
     * @param elements the table to add to this one.
     */
    public void add(ElementTable elements) {
        elements.entrySet().forEach(kv -> {
            put(kv.getKey(), get(kv.getKey()) + kv.getValue());
        });
    }

    /**
     * Add all alements and counts to those in this element table, but use the
     * count argument to multiply count values in the provided table.
     *
     * @param elements
     * @param multiplier
     */
    public void add(ElementTable elements, int multiplier) {
        elements.entrySet().forEach(kv -> {
            put(kv.getKey(), get(kv.getKey()) + kv.getValue() * multiplier);
        });
    }

    /**
     * Copy all entries in this element table. The copy will be completely
     * independent of this instance.
     *
     * @return a copy of this element table.
     */
    public ElementTable copy() {
        ElementTable e = new ElementTable();
        entrySet().forEach(kv -> {
            e.put(kv.getKey(), kv.getValue());
        });
        return e;
    }
    
    /**
     * Returns the sum formula for all elements in this table.
     *
     * @return the sum formula. Returns an empty string if the table is empty.
     */
    public String getSumFormula() {
        StringBuilder ss = new StringBuilder();
        Elements.ELEMENT_ORDER.stream().map(e -> {
            if (get(e) > 0) {
                ss.append(Elements.ELEMENT_SHORTCUT.get(e));
            }
            return e;
        }).filter(e -> (get(e) > 1)).forEachOrdered(e -> {
            ss.append(get(e));
        });
        return ss.toString();
    }

    /**
     * Returns the individual total mass for the provided element.
     *
     * @param element the element to calculate the total mass for.
     * @return the total mass for the given element, or 0.
     */
    public Double getMass(Element element) {
        Integer count = getOrDefault(element, 0);
        return count.doubleValue() * Elements.ELEMENT_MASSES.get(element);
    }

    /**
     * Returns the total summed mass per number of elements.
     *
     * @return the total summed mass for this element table. Returns 0 if the
     * table is empty.
     */
    public Double getMass() {
        return keySet().stream().map((key) -> {
            return getMass(key);
        }).reduce(0.0d, Double::sum);
    }
    
    /**
     * Returns the total summed mass per number of elements, corrected for charge counts * electron rest mass.
     *
     * @param charge the charge of the molecule
     * @return the total summed mass for this element table. Returns 0 if the
     * table is empty.
     */
    public Double getChargedMass(int charge) {
        Double mass = getMass();
        if (charge != 0) {
            mass = (mass - charge * Elements.ELECTRON_REST_MASS) / Math.abs(charge);
        }
        return mass;
    }
}
