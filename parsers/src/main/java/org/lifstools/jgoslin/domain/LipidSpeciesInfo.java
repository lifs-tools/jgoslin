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
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * This class summarizes the FA parts of a lipid, independent of its head group.
 * Thus, it accounts the total number of carbon atoms, double bonds, the number
 * of hydroxylations, the overall FA-headgroup bond type, e.g. ETHER PLASMANYL /
 * PLASMENYL, if any of a lipid's FA chains has such a bond type, or ESTER, AMINE, LCB or
 * UNDEFINED for other cases.
 * 
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class LipidSpeciesInfo extends FattyAcid {

    private LipidLevel level;
    public int numEthers;
    public int numSpecifiedFa;
    public int possFa;
    public int totalFa;
    public LipidFaBondType extendedClass;
    public static final String[] ETHER_PREFIX = {"", "O-", "dO-", "tO-", "eO-"};
    public int lipidClass;

    public LipidSpeciesInfo(int _lipid_class, KnownFunctionalGroups knownFunctionalGroups) {
        super("info", knownFunctionalGroups);
        lipidClass = _lipid_class;
        level = LipidLevel.NO_LEVEL;
        numEthers = 0;
        numSpecifiedFa = 0;
        extendedClass = LipidFaBondType.ESTER;
        totalFa = (LipidClasses.getInstance().size() > lipidClass) ? LipidClasses.getInstance().get(lipidClass).maxNumFa : 0;
        possFa = (LipidClasses.getInstance().size() > lipidClass) ? LipidClasses.getInstance().get(lipidClass).possibleNumFa : 0;
    }

    @Override
    public LipidSpeciesInfo copy() {
        LipidSpeciesInfo lsi = new LipidSpeciesInfo(lipidClass, knownFunctionalGroups);
        lsi.level = level;
        lsi.numEthers = numEthers;
        lsi.numSpecifiedFa = numSpecifiedFa;
        lsi.setPosition(getPosition());
        lsi.totalFa = totalFa;
        lsi.extendedClass = extendedClass;
        lsi.numCarbon = numCarbon;
        lsi.doubleBonds = doubleBonds.copy();
        lsi.lipidFaBondType = lipidFaBondType;

        functionalGroups.entrySet().stream().map(kv -> {
            lsi.functionalGroups.put(kv.getKey(), new ArrayList<>());
            return kv;
        }).forEachOrdered(kv -> {
            kv.getValue().forEach(func_group -> {
                lsi.functionalGroups.get(kv.getKey()).add(func_group.copy());
            });
        });
        return lsi;
    }

    @JsonIgnore
    @Override
    public ElementTable getElements() {
        ElementTable elements = super.computeAndCopyElements();
        if (lipidFaBondType != LipidFaBondType.LCB_EXCEPTION) {
            elements.put(Element.O, elements.get(Element.O) - ((numEthers == 0) ? 1 : 0));
        }
        elements.put(Element.H, elements.get(Element.H) + (numEthers == 0 ? 1 : -1));

        return elements;
    }

    public void add(FattyAcid _fa) {
        if (_fa.lipidFaBondType == LipidFaBondType.ETHER_PLASMENYL || _fa.lipidFaBondType == LipidFaBondType.ETHER_PLASMANYL) {
            numEthers += 1;
            lipidFaBondType = LipidFaBondType.ETHER_PLASMANYL;
            extendedClass = _fa.lipidFaBondType;
        } else if (_fa.lipidFaBondType == LipidFaBondType.LCB_EXCEPTION || _fa.lipidFaBondType == LipidFaBondType.LCB_REGULAR) {
            lipidFaBondType = _fa.lipidFaBondType;
        } else {
            numSpecifiedFa += 1;
        }
        for (Entry<String, ArrayList<FunctionalGroup>> kv : _fa.functionalGroups.entrySet()) {
            if (!functionalGroups.containsKey(kv.getKey())) {
                functionalGroups.put(kv.getKey(), new ArrayList<FunctionalGroup>());
            }
            for (FunctionalGroup func_group : kv.getValue()) {
                functionalGroups.get(kv.getKey()).add(func_group.copy());
            }
        }

        ElementTable e = _fa.computeAndCopyElements();
        numCarbon += e.get(Element.C);
        doubleBonds.setNumDoubleBonds(doubleBonds.getNumDoubleBonds() + _fa.getNDoubleBonds());

    }

    @Override
    public String toString() {
        StringBuilder info_string = new StringBuilder();
        info_string.append(ETHER_PREFIX[numEthers]);
        info_string.append(numCarbon).append(":").append(doubleBonds.getNumDoubleBonds());

        ElementTable fg_elements = getFunctionalGroupElements();
        for (int i = 2; i < Elements.ELEMENT_ORDER.size(); ++i) {
            Element e = Elements.ELEMENT_ORDER.get(i);
            if (fg_elements.get(e) > 0) {
                info_string.append(";").append(Elements.ELEMENT_SHORTCUT.get(e));
                if (fg_elements.get(e) > 1) {
                    info_string.append(fg_elements.get(e));
                }
            }
        }

        return info_string.toString();
    }

    public LipidLevel getLevel() {
        return level;
    }

    public void setLevel(LipidLevel level) {
        this.level = level;
    }

    public int getNumEthers() {
        return numEthers;
    }

    public void setNumEthers(int numEthers) {
        this.numEthers = numEthers;
    }

    public int getNumSpecifiedFa() {
        return numSpecifiedFa;
    }

    public void setNumSpecifiedFa(int numSpecifiedFa) {
        this.numSpecifiedFa = numSpecifiedFa;
    }

    public int getTotalFa() {
        return totalFa;
    }

    public void setTotalFa(int totalFa) {
        this.totalFa = totalFa;
    }

    public LipidFaBondType getExtendedClass() {
        return extendedClass;
    }

    public void setExtendedClass(LipidFaBondType extendedClass) {
        this.extendedClass = extendedClass;
    }

    public int getLipidClass() {
        return lipidClass;
    }

    public void setLipidClass(int lipidClass) {
        this.lipidClass = lipidClass;
    }
    
}
