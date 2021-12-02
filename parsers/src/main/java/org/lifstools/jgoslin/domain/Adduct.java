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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import static java.util.Map.entry;

/**
 * An adduct, generally, consists of a sum formula part, an adduct string, the
 * charge and the charge sign. An example for a valid adduct is : [M+H]1+.
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

    @JsonIgnore
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

    /**
     * Returns the positive elementary charge times the charge sign.
     *
     * @return the net charge.
     */
    public int getCharge() {
        return charge * chargeSign;
    }
}
