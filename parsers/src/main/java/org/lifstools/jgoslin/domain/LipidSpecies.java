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
import java.util.Collection;
import java.util.HashMap;

/**
 * A lipid species is the factual root of the object hierarchy. Lipid category
 * and class are used as taxonomic roots of a lipid species. Partial structural
 * knowledge, apart from the head group, is first encoded in the lipid species.
 *
 * A typical lipid species is PC 32:0 (SwissLipids SLM:000056493), where the
 * head group is defined as PC (Glycerophosphocholines), with fatty acyl chains
 * of unknown individual composition, but known total composition (32 carbon
 * atoms, zero double bonds, no hydroxylations or other functions).
 * 
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LipidSpecies {

    protected Headgroup headGroup;
    protected LipidSpeciesInfo info;

    protected HashMap<String, FattyAcid> fa = new HashMap<>();
    protected ArrayList<FattyAcid> faList = new ArrayList<>();

    public LipidSpecies(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, null, knownFunctionalGroups);
    }

    public LipidSpecies(Headgroup _headGroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        headGroup = _headGroup;

        info = new LipidSpeciesInfo(headGroup.getLipidClass(), knownFunctionalGroups);
        info.setLevel(LipidLevel.SPECIES);

        // add fatty acids
        if (_fa != null && !_fa.isEmpty()) {
            int i = 0;
            FattyAcid fattyAcid = _fa.iterator().next();
            boolean fa_it = (_fa.size() > 0) && (fattyAcid.lipidFaBondType == LipidFaBondType.LCB_EXCEPTION || fattyAcid.lipidFaBondType == LipidFaBondType.LCB_REGULAR);
            for (FattyAcid fatty_acid : _fa) {
                fatty_acid.name = (fa_it && i == 0) ? "LCB" : "FA" + String.valueOf(i + 1 - (fa_it ? 1 : 0));
                fatty_acid.position = -1;
                info.add(fatty_acid);
                ++i;
            }
        }
    }

    public LipidLevel getLipidLevel() {
        return LipidLevel.SPECIES;
    }
    
    public LipidSpeciesInfo getInfo() {
        return info;
    }
    
    public Headgroup getHeadGroup() {
        return headGroup;
    }

    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);
    }

    public String getLipidString(LipidLevel level) {
        switch (level) {
            case UNDEFINED_LEVEL -> throw new ConstraintViolationException("LipidSpecies does not know how to create a lipid string for level " + level.toString());
            case CLASS, CATEGORY -> {
                return headGroup.getLipidString(level);
            }
            case NO_LEVEL, SPECIES -> {
                StringBuilder lipid_string = new StringBuilder();
                lipid_string.append(headGroup.getLipidString(level));

                if (info.getElements().get(Element.C) > 0 || info.numCarbon > 0) {
                    LipidSpeciesInfo lsi = info.copy();
                    for (HeadgroupDecorator decorator : headGroup.getDecorators()) {
                        if (decorator.getName().equals("decorator_alkyl") || decorator.getName().equals("decorator_acyl")) {
                            ElementTable e = decorator.computeAndCopyElements();
                            lsi.numCarbon += e.get(Element.C);
                            lsi.doubleBonds.setNumDoubleBonds(lsi.doubleBonds.getNumDoubleBonds()+ decorator.getNDoubleBonds());
                        }
                    }
                    lipid_string.append(headGroup.getLipidCategory() != LipidCategory.ST ? " " : "/").append(lsi.toString());
                }
                return lipid_string.toString();
            }
            default -> throw new ConstraintViolationException("LipidSpecies does not know how to create a lipid string for level " + level.toString());
        }
    }

    public String getExtendedClass() {
        boolean special_case = (info.numCarbon > 0) ? (headGroup.getLipidCategory() == LipidCategory.GP) : false;
        String class_name = headGroup.getClassName();
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

    @JsonIgnore
    public ElementTable getElements() {

        switch (info.getLevel()) {
            case COMPLETE_STRUCTURE, FULL_STRUCTURE, STRUCTURE_DEFINED, SN_POSITION, MOLECULAR_SPECIES, SPECIES -> {
            }
            default -> throw new LipidException("Element table cannot be computed for lipid level " + info.getLevel().toString());
        }

        if (headGroup.isUseHeadgroup()) {
            throw new LipidException("Element table cannot be computed for lipid level " + info.getLevel().toString());
        }

        ElementTable elements = headGroup.getElements();
        elements.add(info.getElements());

        // since only one FA info is provided, we have to treat this single information as
        // if we would have the complete information about all possible FAs in that lipid
        LipidClassMeta meta = LipidClasses.getInstance().get(headGroup.getLipidClass());

        int additional_fa = meta.possibleNumFa;
        int remaining_H = meta.maxNumFa - additional_fa;
        int hydrochain = meta.specialCases.contains("HC") ? 1 : 0;

        elements.put(Element.O, elements.get(Element.O) - (-additional_fa + info.numEthers + (headGroup.isSpException() ? 1 : 0) + hydrochain));
        elements.put(Element.H, elements.get(Element.H) + (-additional_fa + remaining_H + 2 * info.numEthers + 2 * hydrochain));

        return elements;
    }

}
