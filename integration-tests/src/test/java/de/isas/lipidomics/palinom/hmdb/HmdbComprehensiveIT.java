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

import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.domain.LipidSpecies;
import de.isas.lipidomics.palinom.exceptions.ConstraintViolationException;
import de.isas.lipidomics.palinom.exceptions.ParseTreeVisitorException;
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
public class HmdbComprehensiveIT {

    @ParameterizedTest(name = "{index} ==> ''{0}'' can be parsed with the hmdb grammar")
    @CsvFileSource(resources = "/de/isas/lipidomics/palinom/testfiles/hmdb-parsed-test.csv", numLinesToSkip = 1, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void isValidHmdbName(
            String hmdbName
    ) throws ParsingException, IOException {
        String lipidName = hmdbName;
        HmdbVisitorParser parser = new HmdbVisitorParser();
        LipidAdduct lipidAdduct;
        if (lipidName == null) {
            log.warn("Skipping row, abbreviation was null for name={}", hmdbName);
        } else {
            try {
                lipidAdduct = parser.parse(lipidName);
                LipidSpecies ls = lipidAdduct.getLipid();
                assertNotNull(ls);
            } catch (ConstraintViolationException cex) {
                fail("Parsing current HMDB identifier: " + hmdbName + " failed - name is illegal!" + cex.getMessage());
            } catch (ParsingException ex) {
                fail("Parsing current HMDB identifier: " + hmdbName + " failed - name unsupported in grammar!");
            } catch (ParseTreeVisitorException pve) {
                fail("Parsing current HMDB identifier: " + hmdbName + " failed - incomplete implementation: " + pve.getMessage());
            }
        }
    }
}
