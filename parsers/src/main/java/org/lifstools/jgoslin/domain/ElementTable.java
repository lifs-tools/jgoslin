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

import java.util.EnumMap;

/**
 *
 * @author dominik
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
     * @param elements
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
