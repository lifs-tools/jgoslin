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
package de.isas.lipidomics.palinom.goslin;

import de.isas.lipidomics.domain.HeadGroup;
import de.isas.lipidomics.domain.LipidFaBondType;
import de.isas.lipidomics.domain.LipidLevel;
import de.isas.lipidomics.palinom.ParserRuleContextHandler;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.domain.LipidSpeciesInfo;
import de.isas.lipidomics.palinom.GoslinParser;
import de.isas.lipidomics.palinom.GoslinParser.Lipid_pureContext;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Handler implementation for Sterollipids.
 *
 * @author  nils.hoffmann
 */
class SterolLipidHandler implements ParserRuleContextHandler<Lipid_pureContext, LipidSpecies> {

    private final StructuralSubspeciesFasHandler ssfh;

    public SterolLipidHandler(StructuralSubspeciesFasHandler ssfh) {
        this.ssfh = ssfh;
    }

    @Override
    public LipidSpecies handle(Lipid_pureContext t) {
        return handleSterol(t).orElse(LipidSpecies.NONE);
    }

    private Optional<LipidSpecies> handleSterol(GoslinParser.Lipid_pureContext ctx) throws RuntimeException {
        if (ctx.sterol().stc() != null) {
            LipidSpeciesInfo lsi = new LipidSpeciesInfo(LipidLevel.SPECIES, 0, 0, 0, LipidFaBondType.UNDEFINED);
            HeadGroup headGroup = new HeadGroup(ctx.sterol().stc().st().getText());
            return Optional.of(new LipidSpecies(headGroup, Optional.of(lsi)));
        } else if (ctx.sterol().ste() != null) {
            return Optional.of(handleSte(ctx.sterol().ste()).orElse(LipidSpecies.NONE));
        } else if (ctx.sterol().stes() != null) {
            return Optional.of(handleStes(ctx.sterol().stes()).orElse(LipidSpecies.NONE));
        } else {
            throw new ParseTreeVisitorException("Unhandled sterol lipid: " + ctx.sterol().getText());
        }
    }

    private Optional<LipidSpecies> handleSte(GoslinParser.SteContext che) {
        HeadGroup headGroup = new HeadGroup(che.hg_stc().getText());
        if (che.fa() != null) {
            return ssfh.visitStructuralSubspeciesFas(headGroup, Arrays.asList(che.fa()));
        } else {
            throw new ParseTreeVisitorException("Unhandled context state in Ste!");
        }
    }

    private Optional<LipidSpecies> handleStes(GoslinParser.StesContext che) {
        HeadGroup headGroup = new HeadGroup(che.hg_stcs().getText());
        if (che.fa() != null) {
            return ssfh.visitStructuralSubspeciesFas(headGroup, Arrays.asList(che.fa()));
        } else {
            throw new ParseTreeVisitorException("Unhandled context state in Stes!");
        }
    }
}
