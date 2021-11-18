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

import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public final class LipidAdduct {

    private LipidSpecies lipid;
    private Adduct adduct;

    public LipidAdduct() {
        lipid = null;
        adduct = null;
    }
    
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

    public boolean isLyso() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Lyso") : false;
    }

    public boolean isCardioLipin() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Cardio") : false;
    }

    public boolean containsSugar() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Sugar") : false;
    }

    public boolean containsEster() {
        return (LipidClasses.getInstance().size() > lipid.headGroup.getLipidClass()) ? LipidClasses.getInstance().get(lipid.headGroup.getLipidClass()).specialCases.contains("Ester") : false;
    }

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

    public void setLipid(LipidSpecies lipid) {
        this.lipid = lipid;
    }

    public Adduct getAdduct() {
        return adduct;
    }

    public void setAdduct(Adduct adduct) {
        this.adduct = adduct;
    }
    
}
