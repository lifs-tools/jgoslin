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
import java.util.Collection;
import java.util.HashMap;
import lombok.Data;

/**
 *
 * @author dominik
 */
@Data
public class LipidSpecies {

    protected Headgroup headgroup;
    protected LipidSpeciesInfo info;

    protected HashMap<String, FattyAcid> fa = new HashMap<>();
    protected ArrayList<FattyAcid> faList = new ArrayList<>();

    public LipidSpecies(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, null, knownFunctionalGroups);
    }

    public LipidSpecies(Headgroup _headgroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        headgroup = _headgroup;

        info = new LipidSpeciesInfo(headgroup.lipidClass, knownFunctionalGroups);
        info.level = LipidLevel.SPECIES;

        // add fatty acids
        if (_fa != null) {
            for (FattyAcid fatty_acid : _fa) {
                info.add(fatty_acid);
                faList.add(fatty_acid);
            }
        }
    }

    public LipidLevel getLipidLevel() {
        return LipidLevel.SPECIES;
    }

    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);
    }

    public String getLipidString(LipidLevel level) {
        switch (level) {

            default:
                throw new RuntimeException("LipidSpecies does not know how to create a lipid string for level " + level.toString());

            case UNDEFINED_LEVEL:
                throw new RuntimeException("LipidSpecies does not know how to create a lipid string for level " + level.toString());

            case CLASS:
            case CATEGORY:
                return headgroup.getLipidString(level);

            case NO_LEVEL:
            case SPECIES:
                StringBuilder lipid_string = new StringBuilder();
                lipid_string.append(headgroup.getLipidString(level));

                if (info.elements.get(Element.C) > 0 || info.numCarbon > 0) {
                    LipidSpeciesInfo lsi = info.copy();
                    for (HeadgroupDecorator decorator : headgroup.decorators) {
                        if (decorator.name.equals("decorator_alkyl") || decorator.name.equals("decorator_acyl")) {
                            ElementTable e = decorator.getElements();
                            lsi.numCarbon += e.get(Element.C);
                            lsi.doubleBonds.numDoubleBonds += decorator.getDoubleBonds();
                        }
                    }
                    lipid_string.append(headgroup.lipidCategory != LipidCategory.ST ? " " : "/").append(lsi.toString());
                }
                return lipid_string.toString();
        }
    }

    public String getExtendedClass() {
        boolean special_case = (info.numCarbon > 0) ? (headgroup.lipidCategory == LipidCategory.GP) : false;
        String class_name = headgroup.getClassName();
        if (special_case && (info.extendedClass == LipidFaBondType.ETHER_PLASMANYL || info.extendedClass == LipidFaBondType.ETHER_UNSPECIFIED)) {
            return class_name + "-O";
        } else if (special_case && info.extendedClass == LipidFaBondType.ETHER_PLASMENYL) {
            return class_name + "-P";
        }

        return class_name;
    }

    public ArrayList<FattyAcid> getFaList() {
        return faList;
    }

    public ElementTable getElements() {

        switch (info.level) {
            case COMPLETE_STRUCTURE:
            case FULL_STRUCTURE:
            case STRUCTURE_DEFINED:
            case SN_POSITION:
            case MOLECULAR_SPECIES:
            case SPECIES:
                break;

            default:
                throw new LipidException("Element table cannot be computed for lipid level " + info.level.toString());
        }

        if (headgroup.useHeadgroup) {
            throw new LipidException("Element table cannot be computed for lipid level " + info.level.toString());
        }

        ElementTable elements = headgroup.getElements();
        elements.add(info.getElements());

        // since only one FA info is provided, we have to treat this single information as
        // if we would have the complete information about all possible FAs in that lipid
        LipidClassMeta meta = LipidClasses.getInstance().get(headgroup.lipidClass);

        int additional_fa = meta.possibleNumFa;
        int remaining_H = meta.maxNumFa - additional_fa;
        int hydrochain = meta.specialCases.contains("HC") ? 1 : 0;

        elements.put(Element.O, elements.get(Element.O) - (-additional_fa + info.numEthers + (headgroup.spException ? 1 : 0) + hydrochain));
        elements.put(Element.H, elements.get(Element.H) + (-additional_fa + remaining_H + 2 * info.numEthers + 2 * hydrochain));

        return elements;
    }
}
