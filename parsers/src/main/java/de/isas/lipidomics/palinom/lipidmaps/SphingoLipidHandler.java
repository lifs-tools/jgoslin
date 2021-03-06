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

import de.isas.lipidomics.domain.HeadGroup;
import de.isas.lipidomics.palinom.ParserRuleContextHandler;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.LipidMapsParser.Lipid_pureContext;
import de.isas.lipidomics.palinom.LipidMapsParser;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Handler implementation for Sphingolipids.
 *
 * @author  nils.hoffmann
 */
class SphingoLipidHandler implements ParserRuleContextHandler<Lipid_pureContext, LipidSpecies> {

    private final StructuralSubspeciesLcbHandler sslh;
    private final FattyAcylHandler fhf;

    public SphingoLipidHandler(StructuralSubspeciesLcbHandler sslh, FattyAcylHandler fhf) {
        this.sslh = sslh;
        this.fhf = fhf;
    }

    @Override
    public LipidSpecies handle(Lipid_pureContext t) {
        return handleSphingolipid(t).orElse(LipidSpecies.NONE);
    }

    private Optional<LipidSpecies> handleSphingolipid(LipidMapsParser.Lipid_pureContext ctx) throws RuntimeException {
        if (ctx.sl().dsl() != null) {
            return handleDsl(ctx.sl().dsl());
        } else if (ctx.sl().lsl() != null) {
            return handleLsl(ctx.sl().lsl());
        } else {
            throw new RuntimeException("Unhandled sphingolipid: " + ctx.sl().getText());
        }
    }

    private Optional<LipidSpecies> handleDsl(LipidMapsParser.DslContext dsl) {
        HeadGroup headGroup = new HeadGroup(dsl.hg_dslc().getText());
        if (dsl.dsl_species() != null) { //species level
            //process species level
            return fhf.visitSpeciesLcb(headGroup, dsl.dsl_species().lcb());
        } else if (dsl.dsl_subspecies() != null) {
            //process subspecies
            if (dsl.dsl_subspecies().lcb_fa_sorted() != null) {
                //sorted => StructuralSubspecies
                return sslh.visitStructuralSubspeciesLcb(headGroup, dsl.dsl_subspecies().lcb_fa_sorted().lcb(), Arrays.asList(dsl.dsl_subspecies().lcb_fa_sorted().fa()));
            }
        } else {
            throw new ParseTreeVisitorException("Unhandled context state in DSL!");
        }
        return Optional.empty();
    }

    private Optional<LipidSpecies> handleLsl(LipidMapsParser.LslContext lsl) {
        HeadGroup headGroup = new HeadGroup(lsl.hg_lslc().getText());
        if (lsl.lcb() != null) { //species / subspecies level
            //process structural sub species level
            return sslh.visitStructuralSubspeciesLcb(headGroup, lsl.lcb());
        } else {
            throw new ParseTreeVisitorException("Unhandled context state in LSL!");
        }
    }

}
