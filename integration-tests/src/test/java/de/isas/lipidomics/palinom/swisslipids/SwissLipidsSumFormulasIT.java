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
package de.isas.lipidomics.palinom.swisslipids;

import de.isas.lipidomics.domain.ElementTable;
import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
import de.isas.lipidomics.palinom.exceptions.ParsingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author  nils.hoffmann
 */
@Slf4j
public class SwissLipidsSumFormulasIT {

    @ParameterizedTest(name = "{index} ==> ''{0}'' can be parsed with the swiss lipids grammar and sum formulas match")
    @CsvFileSource(resources = "/de/isas/lipidomics/palinom/formulas-swiss-lipids.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testSumFormulas(String lipidName, String sumFormula) {
        LipidAdduct lipidAdduct;
        try {
            ElementTable referenceTable = new ElementTable(sumFormula);
            SwissLipidsVisitorParser parser = new SwissLipidsVisitorParser();
            try {
                lipidAdduct = parser.parse(lipidName.replaceAll("\"", ""));
                LipidSpecies ls = lipidAdduct.getLipid();
                assertNotNull(ls);
                String prefix = "";
                ElementTable difference = lipidAdduct.getElements().subtract(referenceTable);
                if (difference.isEmpty()) {
                    prefix = "-";
                    difference = referenceTable.subtract(lipidAdduct.getElements());
                }
                assertEquals(sumFormula.replaceAll("\"", ""), lipidAdduct.getSumFormula(), "for lipid name " + lipidName + " with difference " + prefix + difference.getSumFormula());
            } catch (ParseTreeVisitorException pve) {
                fail("Parsing current SwissLipids identifier: " + lipidName + " failed - incomplete implementation: " + pve.getMessage());
            } catch (ParsingException | RuntimeException ex) {
                fail("Parsing current SwissLipids identifier: " + lipidName + " failed - name unsupported in grammar!");
            }
        } catch (ParsingException ex) {
            fail("Could not parse sum formula: " + ex.getMessage());
        }
    }

}
