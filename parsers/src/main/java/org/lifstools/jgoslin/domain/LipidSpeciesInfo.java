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

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class LipidSpeciesInfo extends FattyAcid {

    public LipidLevel level;
    public int numEthers;
    public int numSpecifiedFa;
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
}
