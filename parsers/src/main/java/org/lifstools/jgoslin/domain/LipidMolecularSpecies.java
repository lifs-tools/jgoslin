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
import java.util.Collection;
import java.util.Collections;

/**
 * Molecular species level according to the 2020 update of the Liebisch shorthand
 * nomenclature.
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 * @see LipidLevel
 */
public class LipidMolecularSpecies extends LipidSpecies {

    public LipidMolecularSpecies(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, Collections.emptyList(), knownFunctionalGroups);
    }

    public LipidMolecularSpecies(Headgroup _headgroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        super(_headgroup, _fa, knownFunctionalGroups);
        info.setLevel(LipidLevel.MOLECULAR_SPECIES);
        for (FattyAcid fatty_acid : _fa){
            if (fa.containsKey(fatty_acid.getName())) {
                throw new ConstraintViolationException("FA names must be unique! FA with name " + fatty_acid.getName() + " was already added!");
            }
            fa.put(fatty_acid.getName(), fatty_acid);
            faList.add(fatty_acid);
        }
        

        // add 0:0 dummys
        for (int i = _fa.size(); i < info.totalFa; ++i) {
            FattyAcid fatty_acid = new FattyAcid("FA" + Integer.toString(i + 1), knownFunctionalGroups);
            fatty_acid.position = -1;
            info.add(fatty_acid);
            fa.put(fatty_acid.getName(), fatty_acid);
            faList.add(fatty_acid);
        }
    }

    public String buildLipidSubspeciesName() {
        return buildLipidSubspeciesName(LipidLevel.NO_LEVEL);
    }

    public String buildLipidSubspeciesName(LipidLevel level) {
        if (level == LipidLevel.NO_LEVEL) {
            level = LipidLevel.MOLECULAR_SPECIES;
        }

        String fa_separator = (level != LipidLevel.MOLECULAR_SPECIES || headGroup.getLipidCategory() == LipidCategory.SP) ? "/" : "_";
        StringBuilder lipid_name = new StringBuilder();
        lipid_name.append(headGroup.getLipidString(level));

        String fa_headgroup_separator = (headGroup.getLipidCategory() != LipidCategory.ST) ? " " : "/";

        switch (level) {
            case COMPLETE_STRUCTURE, FULL_STRUCTURE, STRUCTURE_DEFINED, SN_POSITION -> {
                if (faList.size() > 0) {
                    lipid_name.append(fa_headgroup_separator);
                    int i = 0;

                    for (FattyAcid fatty_acid : faList) {
                        if (i++ > 0) {
                            lipid_name.append(fa_separator);
                        }
                        lipid_name.append(fatty_acid.toString(level));
                    }
                }
            }
            default -> {
                boolean go_on = false;
                for (FattyAcid fatty_acid : faList) {
                    if (fatty_acid.numCarbon > 0) {
                        go_on = true;
                        break;
                    }
                }

                if (go_on) {
                    lipid_name.append(fa_headgroup_separator);
                    int i = 0;
                    for (FattyAcid fatty_acid : faList) {
                        if (fatty_acid.numCarbon > 0) {
                            if (i++ > 0) {
                                lipid_name.append(fa_separator);
                            }
                            lipid_name.append(fatty_acid.toString(level));
                        }
                    }
                }
            }
        }
        return lipid_name.toString();
    }

    @Override
    public LipidLevel getLipidLevel() {
        return LipidLevel.MOLECULAR_SPECIES;
    }

    @JsonIgnore
    @Override
    public ElementTable getElements() {
        ElementTable elements = headGroup.getElements();

        // add elements from all fatty acyl chains
        faList.forEach(fatty_acid -> {
            elements.add(fatty_acid.computeAndCopyElements());
        });

        return elements;
    }

    @Override
    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);

    }

    @Override
    public String getLipidString(LipidLevel level) {
        switch (level) {
            case NO_LEVEL, MOLECULAR_SPECIES -> {
                return buildLipidSubspeciesName(LipidLevel.MOLECULAR_SPECIES);
            }
            case CATEGORY, CLASS, SPECIES -> {
                return super.getLipidString(level);
            }

            default -> throw new IllegalArgumentException("LipidMolecularSpecies does not know how to create a lipid string for level " + level.toString());
        }
    }
}
