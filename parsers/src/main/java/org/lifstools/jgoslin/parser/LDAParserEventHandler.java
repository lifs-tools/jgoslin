/*
 * Copyright 2026 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.parser;

import java.util.ArrayList;
import java.util.Map;
import static java.util.Map.entry;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidParsingException;

/**
 * Event handler implementation for the {@link LDAParser}, the sphingolipid
 * names written by LipidDataAnalyzer (LDA), e.g. 'Cer d18:1/n16:0'. The long
 * chain base carries the number of hydroxy groups as prefix (m, d, t, q, p, h
 * = 1 to 6), the N-acyl chain as n, h, i, k (0 to 3 hydroxy groups). These are
 * the default encodings of LDA, see lcbHydroxylationEncoding.txt and
 * faHydroxylationEncoding.txt in https://github.com/ThallingerLab/LDA2.
 * It reuses the {@link GoslinParserEventHandler} for building the lipid and
 * only registers the events of the LDA grammar.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LDAParserEventHandler extends GoslinParserEventHandler {

    // LDA head group -> Goslin class name
    private static final Map<String, String> HEADGROUPS = Map.of("SphBase", "SPB", "Cer1P", "CerP");
    // default LDA encodings, see lcbHydroxylationEncoding.txt and
    // faHydroxylationEncoding.txt in https://github.com/ThallingerLab/LDA2
    private static final Map<String, Integer> LCB_HYDROXYLS = Map.of("m", 1, "d", 2, "t", 3, "q", 4, "p", 5, "h", 6);
    private static final Map<String, Integer> FA_HYDROXYLS = Map.of("n", 0, "h", 1, "i", 2, "k", 3);

    /**
     * Create a new {@code LDAParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public LDAParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents = Map.ofEntries(
                    entry("lipid_pre_event", this::resetParser),
                    entry("lipid_post_event", this::buildLipid),
                    entry("hg_lsl_pre_event", this::setLdaHeadGroupName),
                    entry("hg_dsl_pre_event", this::setLdaHeadGroupName),
                    entry("sl_species_pre_event", this::setSpeciesLevel),
                    entry("sl_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("lcb_pre_event", this::newLcb),
                    entry("lcb_post_event", this::cleanLcb),
                    entry("fa_pre_event", this::newFa),
                    entry("fa_post_event", this::appendFa),
                    entry("lcb_hydroxyl_pre_event", this::addLcbHydroxyl),
                    entry("fa_hydroxyl_pre_event", this::addFaHydroxyl),
                    entry("db_count_pre_event", this::addDoubleBonds),
                    entry("carbon_pre_event", this::addCarbon)
            );
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize LDAParserEventHandler");
        }
    }

    private void setLdaHeadGroupName(TreeNode node) {
        String hg = node.getText();
        headGroup = HEADGROUPS.getOrDefault(hg, hg);
    }

    private void addHydroxylCount(int numH) {
        if (numH <= 0) {
            return;
        }
        FunctionalGroup functionalGroup = knownFunctionalGroups.get("OH");
        functionalGroup.setCount(numH);
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get("OH").add(functionalGroup);
    }

    private void addLcbHydroxyl(TreeNode node) {
        int numH = LCB_HYDROXYLS.get(node.getText());
        // the hydroxy group at C1 of a regular long chain base carries the head group
        if (spRegularLcb()) {
            numH -= 1;
        }
        addHydroxylCount(numH);
    }

    private void addFaHydroxyl(TreeNode node) {
        addHydroxylCount(FA_HYDROXYLS.get(node.getText()));
    }

}
