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
import java.util.Map.Entry;

/**
 * Base class for lipid names parsed using the different grammars. This can
 * contain a lipid, an adduct and a sum formula.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class LipidAdduct {

    private final LipidSpecies lipid;
    private final Adduct adduct;

    public LipidAdduct(LipidSpecies lipid, Adduct adduct) {
        this.lipid = lipid;
        this.adduct = adduct;
    }

    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);
    }

    public String getLipidString(LipidLevel level) {
        StringBuilder sb = new StringBuilder();
        if (lipid != null) {
            sb.append(lipid.getLipidString(level));
        } else {
            return "";
        }

        switch (level) {
            case CLASS:
            case CATEGORY:
                break;

            default:
                if (adduct != null) {
                    sb.append(adduct.getLipidString());
                }
                break;
        }

        return sb.toString();
    }

    public String getClassName() {
        return (lipid != null) ? lipid.headGroup.getClassName() : "";
    }

    @JsonIgnore
    public boolean isLyso() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Lyso") : false;
    }

    @JsonIgnore
    public boolean isCardioLipin() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Cardio") : false;
    }

    @JsonIgnore
    public boolean isContainsSugar() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Sugar") : false;
    }

    @JsonIgnore
    public boolean isContainsEster() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Ester") : false;
    }

    @JsonIgnore
    public boolean isSpException() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("SP_Exception") : false;
    }

    public LipidLevel getLipidLevel() {
        return lipid.getLipidLevel();
    }

    public String getExtendedClass() {
        return (lipid != null) ? lipid.getExtendedClass() : "";
    }

    public double getMass() {
        ElementTable elements = getElements();
        int charge = 0;
        double mass = 0;

        if (adduct != null) {
            charge = adduct.getCharge();
        }

        for (Entry<Element, Integer> kvp : elements.entrySet()) {
            mass += Elements.ELEMENT_MASSES.get(kvp.getKey()) * kvp.getValue();
        }

        if (charge != 0) {
            mass = (mass - charge * Elements.ELECTRON_REST_MASS) / Math.abs(charge);
        }

        return mass;
    }

    @JsonIgnore
    public ElementTable getElements() {
        ElementTable elements = new ElementTable();

        if (lipid != null) {
            ElementTable lipid_elements = lipid.getElements();
            for (Entry<Element, Integer> kv : lipid_elements.entrySet()) {
                elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());
            }
        }

        if (adduct != null) {
            ElementTable adduct_elements = adduct.getElements();
            for (Entry<Element, Integer> kv : adduct_elements.entrySet()) {
                elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());
            }
        }
        return elements;
    }

    @Override
    public String toString() {
        return LipidAdduct.this.getLipidString();
    }

    public String getSumFormula() {
        return getElements().getSumFormula();
    }

    public LipidSpecies getLipid() {
        return lipid;
    }

    public Adduct getAdduct() {
        return adduct;
    }

}
