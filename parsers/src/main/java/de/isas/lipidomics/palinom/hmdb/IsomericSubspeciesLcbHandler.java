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
package de.isas.lipidomics.palinom.hmdb;

import de.isas.lipidomics.domain.LipidFaBondType;
import de.isas.lipidomics.domain.LipidIsomericSubspecies;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.domain.LipidStructuralSubspecies;
import de.isas.lipidomics.domain.FattyAcid;
import de.isas.lipidomics.domain.FattyAcidType;
import de.isas.lipidomics.domain.HeadGroup;
import de.isas.lipidomics.domain.ModificationsList;
import de.isas.lipidomics.palinom.HandlerUtils;
import de.isas.lipidomics.palinom.HMDBParser;
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

    public Optional<LipidSpecies> visitIsomericSubspeciesLcb(HeadGroup headGroup, HMDBParser.LcbContext lcbContext, List<HMDBParser.FaContext> faContexts) {
        List<FattyAcid> fas = new LinkedList<>();
        FattyAcid lcbA = buildIsomericLcb(headGroup, lcbContext, "LCB", 1);
        fas.add(lcbA);
        int nIsomericFas = 0;
        if (lcbA.getType() == FattyAcidType.ISOMERIC) {
            nIsomericFas++;
        }
        for (int i = 0; i < faContexts.size(); i++) {
            FattyAcid fa = isfh.buildIsomericFa(headGroup, faContexts.get(i), "FA" + (i + 1), i + 2);
            fas.add(fa);
            if (fa.getType() == FattyAcidType.ISOMERIC) {
                nIsomericFas++;
            }
        }
        if (nIsomericFas == fas.size()) {
            FattyAcid[] arrs = new FattyAcid[fas.size()];
            fas.stream().map((t) -> {
                return (FattyAcid) t;
            }).collect(Collectors.toList()).toArray(arrs);
            return Optional.of(new LipidIsomericSubspecies(headGroup, arrs));
        } else {
            FattyAcid[] arrs = new FattyAcid[fas.size()];
            fas.toArray(arrs);
            return Optional.of(new LipidStructuralSubspecies(headGroup, arrs));
        }
    }

    public FattyAcid buildIsomericLcb(HeadGroup headGroup, HMDBParser.LcbContext ctx, String faName, int position) {
        FattyAcid.IsomericFattyAcidBuilder fa = FattyAcid.isomericFattyAcidBuilder();
        LipidFaBondType lfbt = faHelper.getLipidLcbBondType(headGroup, ctx);
        int modificationHydroxyls = 0;
        ModificationsList modifications = new ModificationsList();
        if (ctx.fa_lcb_suffix() != null) {
            modifications = faHelper.resolveModifications(ctx.fa_lcb_suffix());
            modificationHydroxyls += modifications.countFor("OH");
            fa.modifications(modifications);
        }
        if (ctx.lcb_core() != null) {
            fa.nCarbon(HandlerUtils.asInt(ctx.lcb_core().carbon(), 0));
            fa.nHydroxy(modificationHydroxyls + faHelper.getNHydroxyl(ctx));
            if (ctx.lcb_core().db() != null) {
                int doubleBonds = 0;
                if (ctx.lcb_core().db().db_count() != null) {
                    doubleBonds = HandlerUtils.asInt(ctx.lcb_core().db().db_count(), 0);
                }
                if (ctx.lcb_core().db().db_positions() != null) {
                    fa.nDoubleBonds(doubleBonds);
                    fa.doubleBondPositions(faHelper.resolveDoubleBondPositions(lfbt, ctx.lcb_core().db().db_positions()));
                } else { // handle cases like (0:0) but with at least one fa with isomeric subspecies level
                    Map<Integer, String> doubleBondPositions = new LinkedHashMap<>();
                    if (ctx.lcb_core().db().db_count() != null) {
                        if (doubleBonds > 0) {
                            return FattyAcid.structuralFattyAcidBuilder().
                                    lipidFaBondType(lfbt).
                                    name(faName).
                                    lcb(true).
                                    nHydroxy(faHelper.getNHydroxyl(ctx)).
                                    nCarbon(HandlerUtils.asInt(ctx.lcb_core().carbon(), 0)).
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
    }
}
