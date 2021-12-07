/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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

import org.lifstools.jgoslin.domain.LipidAdduct;
import java.util.Arrays;
import java.util.List;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.StringFunctions;
import org.springframework.core.io.ClassPathResource;

/**
 * Implementation that uses all available parsers to parse a given lipid name.
 * First successful parser implementation wins.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LipidParser {

    private final List<Parser<LipidAdduct>> parserList;
    private Parser<LipidAdduct> lastSuccessfulParser = null;

    private LipidParser(Parser<LipidAdduct>... parsers) {
        parserList = Arrays.asList(parsers);
    }

    /**
     * Create a new lipid parser instance.
     *
     */
    public LipidParser() {
        this(new KnownFunctionalGroups(StringFunctions.getResourceAsStringList(new ClassPathResource("functional-groups.csv")), new SumFormulaParser()));
    }

    /**
     * Create a new lipid parser instance.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public LipidParser(KnownFunctionalGroups knownFunctionalGroups) {
        this(
                new ShorthandParser(knownFunctionalGroups),
                new FattyAcidParser(knownFunctionalGroups),
                new GoslinParser(knownFunctionalGroups),
                new LipidMapsParser(knownFunctionalGroups),
                new SwissLipidsParser(knownFunctionalGroups),
                new HmdbParser(knownFunctionalGroups)
        );
    }

    /**
     * This method tries multiple parsers in a defined order to parse the
     * provided lipid name.If no parser is able to parse the name successfully,
     * an exception is thrown.
     *
     * @param lipidName the lipid name to parse.
     * @return the {@link LipidAdduct} if parsing with at least one parser
     * succeeded.
     * @throws LipidParsingException if now parser was able to parse the
     * provided lipid name.
     */
    public LipidAdduct parse(String lipidName) {
        lastSuccessfulParser = null;
        Parser<LipidAdduct> lastParser = null;
        BaseParserEventHandler<LipidAdduct> eventHandler = null;
        for (Parser<LipidAdduct> parser : parserList) {
            lastParser = parser;
            eventHandler = parser.newEventHandler();
            LipidAdduct lipid = parser.parse(lipidName, eventHandler, false);
            if (lipid != null) {
                lastSuccessfulParser = parser;
                return lipid;
            }
        }
        String message = " Parsing failed ";
        if (eventHandler == null) {
            message += " with unknown reason.";
        } else {
            String errorMessage = eventHandler.errorMessage;
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = lipidName;
            }
            message += ("at or after " + errorMessage);
        }
        throw new LipidParsingException("Could not parse lipid '" + lipidName + "' with any parser!" + message);
    }

    /**
     * Returns the last successful parser instance. May be null, if either no
     * parser has been applied yet, or no parser has been successfully applied
     * for parsing the last lipid name.
     *
     * @return the last successful parser instance.
     */
    public Parser<LipidAdduct> getLastSuccessfulParser() {
        return lastSuccessfulParser;
    }

}
