/*
 * Copyright 2019 nils.hoffmann.
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
package de.isas.lipidomics.domain;

import de.isas.lipidomics.palinom.exceptions.ConstraintViolationException;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A structural subspecies. Child of LipidMolecularSubspecies. Individual FAs
 * and their sn positions are known, but not the double bond positions.
 *
 * Example: Phosphatidylinositol (8:0/8:0) or PI(8:0/8:0)
 *
 * @author nils.hoffmann
 * @see LipidMolecularSubspecies
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LipidStructuralSubspecies extends LipidMolecularSubspecies {

    @Builder(builderMethodName = "lipidStructuralSubspeciesBuilder")
    public LipidStructuralSubspecies(HeadGroup headGroup, FattyAcid... fa) {
        super(headGroup);
        int nCarbon = 0;
        int nHydroxyl = 0;
        int nDoubleBonds = 0;
        ModificationsList mods = new ModificationsList();
        for (FattyAcid fas : fa) {
            if (super.fa.containsKey(fas.getName())) {
                throw new ConstraintViolationException(
                        "FA names must be unique! FA with name " + fas.getName() + " was already added!");
            } else {
                super.fa.put(fas.getName(), fas);
                nCarbon += fas.getNCarbon();
                nHydroxyl += fas.getNHydroxy();
                nDoubleBonds += fas.getNDoubleBonds();
                mods.addAll(fas.getModifications());
            }
        }
        super.info = LipidSpeciesInfo.lipidSpeciesInfoBuilder().
                level(LipidLevel.STRUCTURAL_SUBSPECIES).
                name(headGroup.getName()).
                position(-1).
                nCarbon(nCarbon).
                nHydroxy(nHydroxyl).
                nDoubleBonds(nDoubleBonds).
                lipidFaBondType(LipidFaBondType.getLipidFaBondType(headGroup, fa)).
                modifications(mods).
                build();
    }

    @Override
    public String getLipidString(LipidLevel level) {
        return this.getLipidString(level, false);
    }

    @Override
    public String getLipidString(LipidLevel level, boolean normalizeHeadGroup) {
        String headGroup = normalizeHeadGroup ? getNormalizedHeadGroup() : getHeadGroup().getName();
        switch (level) {
            case STRUCTURAL_SUBSPECIES:
                return super.buildLipidSubspeciesName(level, "/", headGroup, normalizeHeadGroup);
            case MOLECULAR_SUBSPECIES:
            case CATEGORY:
            case CLASS:
            case SPECIES:
                return super.getLipidString(level, normalizeHeadGroup);
            default:
                LipidLevel thisLevel = getInfo().getLevel();
                throw new ConstraintViolationException(getClass().getSimpleName() + " can not create a normalized string for lipid with level " + thisLevel + " for level " + level + ": target level is more specific than this lipid's level!");
        }
    }

    @Override
    public String getNormalizedLipidString() {
        return getLipidString(getInfo().getLevel(), true);
    }

    @Override
    public String toString() {
        return getLipidString();
    }
}
