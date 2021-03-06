/*
 * Copyright 2020  nils.hoffmann.
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
package de.isas.lipidomics.palinom.lipidmaps;

import de.isas.lipidomics.domain.LipidFaBondType;
import de.isas.lipidomics.domain.LipidIsomericSubspecies;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.domain.LipidStructuralSubspecies;
import de.isas.lipidomics.domain.FattyAcid;
import de.isas.lipidomics.domain.FattyAcidType;
import de.isas.lipidomics.domain.HeadGroup;
import de.isas.lipidomics.domain.ModificationsList;
import de.isas.lipidomics.palinom.HandlerUtils;
import de.isas.lipidomics.palinom.LipidMapsParser;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handler for Isomeric LCBs.
 *
 * @author nils.hoffmann
 */
class IsomericSubspeciesLcbHandler {

    private final IsomericSubspeciesFasHandler isfh;
    private final FattyAcylHelper faHelper;

    public IsomericSubspeciesLcbHandler(IsomericSubspeciesFasHandler isfh, FattyAcylHelper faHelper) {
        this.isfh = isfh;
        this.faHelper = faHelper;
    }

    public FattyAcid buildIsomericLcb(HeadGroup headGroup, LipidMapsParser.LcbContext ctx, String faName, int position) {
        FattyAcid.IsomericFattyAcidBuilder fa = FattyAcid.isomericFattyAcidBuilder();
        LipidFaBondType lfbt = faHelper.getLipidLcbBondType(ctx);
        if (ctx.lcb_fa() != null) {
            int modificationHydroxyls = 0;
            ModificationsList modifications = new ModificationsList();
            if (ctx.lcb_fa().lcb_fa_mod() != null) {
                modifications = faHelper.resolveModifications(ctx.lcb_fa().lcb_fa_mod().modification());
                modificationHydroxyls += modifications.countFor("OH");
                fa.modifications(modifications);
            }
            if (ctx.lcb_fa().lcb_fa_unmod() != null) {
                LipidMapsParser.Lcb_fa_unmodContext factx = ctx.lcb_fa().lcb_fa_unmod();
                fa.nCarbon(HandlerUtils.asInt(factx.carbon(), 0));
                fa.nHydroxy(faHelper.getHydroxyCount(ctx) + modificationHydroxyls);
                if (factx.db() != null) {
                    int doubleBonds = 0;
                    if (factx.db().db_count() != null) {
                        doubleBonds = HandlerUtils.asInt(factx.db().db_count(), 0);
                        fa.nDoubleBonds(doubleBonds);
                    }
                    if (factx.db().db_positions() != null) {
                        fa.doubleBondPositions(faHelper.resolveDoubleBondPositions(lfbt, factx.db().db_positions()));
                    } else { // handle cases like (0:0) but with at least one fa with isomeric subspecies level
                        Map<Integer, String> doubleBondPositions = new LinkedHashMap<>();
                        if (factx.db().db_count() != null) {
                            if (doubleBonds > 0) {
                                return FattyAcid.structuralFattyAcidBuilder().
                                        lipidFaBondType(lfbt).
                                        name(faName).
                                        lcb(true).
                                        nHydroxy(faHelper.getHydroxyCount(ctx) + modificationHydroxyls).
                                        nCarbon(HandlerUtils.asInt(factx.carbon(), 0)).
                                        nDoubleBonds(doubleBonds).
                                        position(position).
                                        modifications(modifications).
                                        build();
                            }
                        }
                        fa.doubleBondPositions(doubleBondPositions);
                    }
                }
                fa.lipidFaBondType(lfbt);
                return fa.name(faName).lcb(true).position(position).build();
            } else {
                throw new ParseTreeVisitorException("Uninitialized FaContext!");
            }
        } else {
            throw new ParseTreeVisitorException("Uninitialized LCB FaContext!");
        }
    }
}
