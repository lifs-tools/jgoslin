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

import java.util.Collection;

/**
 *
 * @author dominik
 */
public class LipidMolecularSpecies extends LipidSpecies {

    public LipidMolecularSpecies(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, null, knownFunctionalGroups);
    }

    public LipidMolecularSpecies(Headgroup _headgroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        super(_headgroup, _fa, knownFunctionalGroups);
        info.level = LipidLevel.MOLECULAR_SPECIES;
        faList.stream().map(fatty_acid -> {
            if (fa.containsKey(fatty_acid.name)) {
                throw new ConstraintViolationException("FA names must be unique! FA with name " + fatty_acid.name + " was already added!");
            }
            return fatty_acid;
        }).forEachOrdered(fatty_acid -> {
            fa.put(fatty_acid.name, fatty_acid);
        });

        // add 0:0 dummys
        for (int i = _fa.size(); i < info.totalFa; ++i) {
            FattyAcid fatty_acid = new FattyAcid("FA" + Integer.toString(i + _fa.size() + 1), knownFunctionalGroups);
            info.add(fatty_acid);
            fa.put(fatty_acid.name, fatty_acid);
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

        String fa_separator = (level != LipidLevel.MOLECULAR_SPECIES || headGroup.lipidCategory == LipidCategory.SP) ? "/" : "_";
        StringBuilder lipid_name = new StringBuilder();
        lipid_name.append(headGroup.getLipidString(level));

        String fa_headgroup_separator = (headGroup.lipidCategory != LipidCategory.ST) ? " " : "/";

        switch (level) {
            case COMPLETE_STRUCTURE:
            case FULL_STRUCTURE:
            case STRUCTURE_DEFINED:
            case SN_POSITION:
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
                break;

            default:
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
                break;
        }
        return lipid_name.toString();
    }

    @Override
    public LipidLevel getLipidLevel() {
        return LipidLevel.MOLECULAR_SPECIES;
    }

    @Override
    public ElementTable getElements() {
        ElementTable elements = headGroup.getElements();

        // add elements from all fatty acyl chains
        faList.forEach(fatty_acid -> {
            elements.add(fatty_acid.getElements());
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
            case NO_LEVEL:
            case MOLECULAR_SPECIES:
                return buildLipidSubspeciesName(LipidLevel.MOLECULAR_SPECIES);

            case CATEGORY:
            case CLASS:
            case SPECIES:
                return super.getLipidString(level);

            default:
                throw new IllegalArgumentException("LipidMolecularSpecies does not know how to create a lipid string for level " + level.toString());
        }
    }
}
