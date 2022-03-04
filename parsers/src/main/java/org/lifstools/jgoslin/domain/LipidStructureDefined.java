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

import java.util.Collection;
import java.util.Collections;

/**
 * Structure defined level according to the 2020 update of the Liebisch
 * shorthand nomenclature.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 * @see LipidLevel
 */
public class LipidStructureDefined extends LipidSnPosition {

    public LipidStructureDefined(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, Collections.emptyList(), knownFunctionalGroups);
    }

    public LipidStructureDefined(Headgroup _headgroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        super(_headgroup, _fa, knownFunctionalGroups);
        info.setLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    @Override
    public LipidLevel getLipidLevel() {
        return LipidLevel.STRUCTURE_DEFINED;
    }

    @Override
    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);
    }

    @Override
    public String getLipidString(LipidLevel level) {
        switch (level) {
            case NO_LEVEL, STRUCTURE_DEFINED -> {
                return buildLipidSubspeciesName(LipidLevel.STRUCTURE_DEFINED);
            }
            case SN_POSITION, MOLECULAR_SPECIES, CATEGORY, CLASS, SPECIES -> {
                return super.getLipidString(level);
            }

            default ->
                throw new ConstraintViolationException("LipidStructureDefined does not know how to create a lipid string for level " + level.toString());
        }
    }

}
