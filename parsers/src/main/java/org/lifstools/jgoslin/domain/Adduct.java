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

import java.util.Map;
import static java.util.Map.entry;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class Adduct {

    private String sumFormula;
    private String adductString;
    private int charge;
    private int chargeSign;

    public static final Map<String, ElementTable> ADDUCTS = Map.ofEntries(
            entry("+H", ElementTable.of(Map.entry(Element.H, 1))),
            entry("+2H", ElementTable.of(Map.entry(Element.H, 2))),
            entry("+3H", ElementTable.of(Map.entry(Element.H, 3))),
            entry("+4H", ElementTable.of(Map.entry(Element.H, 4))),
            entry("-H", ElementTable.of(Map.entry(Element.H, -1))),
            entry("-2H", ElementTable.of(Map.entry(Element.H, -2))),
            entry("-3H", ElementTable.of(Map.entry(Element.H, -3))),
            entry("-4H", ElementTable.of(Map.entry(Element.H, -4))),
            entry("+H-H2O", ElementTable.of(Map.entry(Element.H, -1), Map.entry(Element.O, -1))),
            entry("+NH4", ElementTable.of(Map.entry(Element.N, 1), Map.entry(Element.H, 4))),
            entry("+Cl", ElementTable.of(Map.entry(Element.Cl, 1))),
            entry("+HCOO", ElementTable.of(Map.entry(Element.H, 1), Map.entry(Element.C, 1), Map.entry(Element.O, 2))),
            entry("+CH3COO", ElementTable.of(Map.entry(Element.H, 3), Map.entry(Element.C, 2), Map.entry(Element.O, 2)))
    );

    public static final Map<String, Integer> ADDUCT_CHARGES = Map.ofEntries(
            entry("+H", 1),
            entry("+2H", 2),
            entry("+3H", 3),
            entry("+4H", 4),
            entry("-H", -1),
            entry("-2H", -2),
            entry("-3H", -3),
            entry("-4H", -4),
            entry("+H-H2O", 1),
            entry("+NH4", 1),
            entry("+Cl", -1),
            entry("+HCOO", -1),
            entry("+CH3COO", -1)
    );

    public Adduct(String _sum_formula, String _adduct_string) {
        this(_sum_formula, _adduct_string, 1, 1);

    }

    public Adduct(String _sum_formula, String _adduct_string, int _charge, int _sign) {
        sumFormula = _sum_formula;
        adductString = _adduct_string;
        charge = _charge;
        setChargeSign(_sign);

    }

    public void setSumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
    }

    public String getSumFormula() {
        return this.sumFormula;
    }

    public void setAdductString(String adductString) {
        this.adductString = adductString;
    }

    public String getAdductString() {
        return this.adductString;
    }

    public void setChargeSign(int _sign) {
        if (-1 <= _sign && _sign <= 1) {
            chargeSign = _sign;
        } else {
            throw new ConstraintViolationException("Sign can only be -1, 0, or 1");
        }
    }

    public int getChargeSign() {
        return this.chargeSign;
    }

    public void setCharge(int charge) {
        if (charge > 0) {
            this.chargeSign = 1;
        } else {
            this.chargeSign = -1;
        }
        this.charge = charge;
    }

    public String getLipidString() {
        if (charge == 0) {
            return "[M]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[M").append(sumFormula).append(adductString).append("]").append(charge).append(((chargeSign > 0) ? "+" : "-"));

        return sb.toString();
    }

    public ElementTable getElements() {
        ElementTable elements = new ElementTable();
//        String adduct_name = adductString.substring(1);

        if (ADDUCTS.containsKey(adductString)) {
            if (ADDUCT_CHARGES.get(adductString) != getCharge()) {
                throw new ConstraintViolationException("Provided charge '" + getCharge() + "' in contradiction to adduct '" + adductString + "' charge '" + ADDUCT_CHARGES.get(adductString) + "'.");
            }
            elements.add(ADDUCTS.get(adductString));
        } else {
            throw new ConstraintViolationException("Adduct '" + adductString + "' is unknown.");
        }

        return elements;
    }

    public int getCharge() {
        return charge * chargeSign;
    }
}
