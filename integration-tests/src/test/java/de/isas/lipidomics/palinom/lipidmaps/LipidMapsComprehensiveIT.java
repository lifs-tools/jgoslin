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

import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.exceptions.ParsingException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author nils.hoffmann
 */
@Slf4j
public class LipidMapsComprehensiveIT {

    @ParameterizedTest(name = "{index} ==> ''{0}'' can be parsed with the lipidMaps grammar")
    @CsvFileSource(resources = "/de/isas/lipidomics/palinom/lipidmaps-names-Feb-10-2020.tsv", numLinesToSkip = 1, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void isValidLipidMapsName(
            String lipidMapsId,
            String lipidMapsName,
            String systematicName,
            String abbreviation,
            String lipidMapsCategory,
            String lipidMapsMainClass,
            String lipidMapsSubClass
    ) throws ParsingException, IOException {
        if (abbreviation == null) {
            log.info("Entry " + lipidMapsId + " had no abbreviation! Skipping!");
            return;
        }
        log.info("Parsing current lipid maps identifier: {}", abbreviation);

        if ("MGDG(18:0(9Z)/18:2(9Z,12Z))".equals(abbreviation)) {
            log.info("Skipping test for " + abbreviation + ". Illegal entry with double bond positions on FA chain with no double bonds!");
            return;
        }
        if ("Isocaldarchaeol(0:0)".equals(abbreviation) || "PC(O-5:0)[R]".equals(abbreviation)) {
            log.info("Skipping test for " + abbreviation + ". Currently unsupported!");
            return;
        }
        LipidMapsVisitorParser parser = new LipidMapsVisitorParser();
        LipidAdduct lipidAdduct;
        try {
            lipidAdduct = parser.parse(abbreviation);
            LipidSpecies ls = lipidAdduct.getLipid();
            assertNotNull(ls);
        } catch (RuntimeException rex) {
            rex.printStackTrace();
            fail("Parsing current LipidMAPS identifier: " + abbreviation + " failed - missing implementation!");
        } catch (ParsingException ex) {
            log.warn("Parsing current LipidMAPS identifier: " + abbreviation + " failed - name unsupported in grammar!");
        }
    }
}
