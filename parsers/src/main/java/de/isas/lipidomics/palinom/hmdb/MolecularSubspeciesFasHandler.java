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
import de.isas.lipidomics.domain.LipidMolecularSubspecies;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.domain.FattyAcid;
import de.isas.lipidomics.domain.HeadGroup;
import de.isas.lipidomics.domain.ModificationsList;
import de.isas.lipidomics.palinom.HandlerUtils;
import de.isas.lipidomics.palinom.HMDBParser;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Handler for Molecular FAs.
 *
 * @author nils.hoffmann
 */
class MolecularSubspeciesFasHandler {

    private final FattyAcylHelper faHelper;

    public MolecularSubspeciesFasHandler(FattyAcylHelper faHelper) {
        this.faHelper = faHelper;
    }

    public Optional<LipidSpecies> visitMolecularSubspeciesFas(HeadGroup headGroup, List<HMDBParser.FaContext> faContexts) {
        List<FattyAcid> fas = new LinkedList<>();
        for (int i = 0; i < faContexts.size(); i++) {
            FattyAcid fa = buildMolecularFa(headGroup, faContexts.get(i), "FA" + (i + 1));
            fas.add(fa);
        }
        FattyAcid[] arrs = new FattyAcid[fas.size()];
        fas.toArray(arrs);
        return Optional.of(new LipidMolecularSubspecies(headGroup, arrs));
    }

    public FattyAcid buildMolecularFa(HeadGroup headGroup, HMDBParser.FaContext ctx, String faName) {
        FattyAcid.MolecularFattyAcidBuilder fa = FattyAcid.molecularFattyAcidBuilder();
        LipidFaBondType lfbt = faHelper.getLipidFaBondType(ctx);
        int modificationHydroxyls = 0;
        ModificationsList modificationsList = new ModificationsList();
        if (ctx.fa_lcb_suffix() != null) {
            modificationsList = faHelper.resolveModifications(ctx.fa_lcb_suffix());
            modificationHydroxyls += modificationsList.countFor("OH");
            fa.modifications(modificationsList);
        }
        if (ctx.fa_core() != null) {
            fa.nCarbon(HandlerUtils.asInt(ctx.fa_core().carbon(), 0));
            if (ctx.fa_core().db() != null) {
                fa.nDoubleBonds(HandlerUtils.asInt(ctx.fa_core().db().db_count(), 0) + (lfbt == LipidFaBondType.ETHER_PLASMENYL ? 1 : 0));
                if (ctx.fa_core().db().db_positions() != null) {
                    throw new RuntimeException("Support for double bond positions is implemented in " + IsomericSubspeciesFasHandler.class.getSimpleName() + "!");
                }
            }
            fa.lipidFaBondType(lfbt);
            return fa.name(faName).nHydroxy(modificationHydroxyls).build();
        } else if (ctx.fa_lcb_prefix() != null || ctx.fa_lcb_suffix() != null) { //handling of lcbs
            throw new RuntimeException("Support for lcbs is implemented in " + StructuralSubspeciesLcbHandler.class.getSimpleName() + "!");
        } else if (ctx.furan_fa() != null) {
            throw new ParseTreeVisitorException("Furan FAs are currently unsupported! Please contact the developers at https://lifs.isas.de/support for assistance.");
        } else {
            throw new ParseTreeVisitorException("Uninitialized FaContext!");
        }
    }
}
