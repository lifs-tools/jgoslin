/*
MIT License

Copyright (c) the authors (listed in global LICENSE file)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
 *
 * @author dominik
 */
public class LipidParser {

    private final List<Parser<LipidAdduct>> parserList;
    private Parser<LipidAdduct> lastSuccessfulParser = null;

    private LipidParser(Parser<LipidAdduct>... parsers) {
        parserList = Arrays.asList(parsers);
    }
    
    public static LipidParser newInstance() {
        SumFormulaParser sfp = SumFormulaParser.newInstance();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList(new ClassPathResource("functional-groups.csv")), sfp);
        return newInstance(knownFunctionalGroups);
    }
    
    public static LipidParser newInstance(KnownFunctionalGroups knownFunctionalGroups) {
        return new LipidParser(knownFunctionalGroups);
    }

    private LipidParser(KnownFunctionalGroups knownFunctionalGroups) {
        this(
                ShorthandParser.newInstance(knownFunctionalGroups, "Shorthand2020.g4", StringFunctions.DEFAULT_QUOTE),
                FattyAcidParser.newInstance(knownFunctionalGroups, "FattyAcids.g4", StringFunctions.DEFAULT_QUOTE),
                GoslinParser.newInstance(knownFunctionalGroups, "Goslin.g4", StringFunctions.DEFAULT_QUOTE),
                LipidMapsParser.newInstance(knownFunctionalGroups, "LipidMaps.g4", StringFunctions.DEFAULT_QUOTE),
                SwissLipidsParser.newInstance(knownFunctionalGroups, "SwissLipids.g4", StringFunctions.DEFAULT_QUOTE),
                HmdbParser.newInstance(knownFunctionalGroups, "HMDB.g4", StringFunctions.DEFAULT_QUOTE)
        );
    }

    /**
     * This method tries multiple parsers in a defined order to parse the
     * provided lipid name.If no parser is able to parse the name successfully,
 an exception is thrown.
     *
     * @param lipid_name the lipid name to parse.
     * @return the {@link LipidAdduct} if parsing with at least one parser
     * succeeded.
     * @throws LipidParsingException if now parser was able to parse the
     * provided lipid name.
     */
    public LipidAdduct parse(String lipid_name) {
        lastSuccessfulParser = null;
        Parser<LipidAdduct> lastParser = null;
        BaseParserEventHandler<LipidAdduct> eventHandler = null;
        for (Parser<LipidAdduct> parser : parserList) {
            lastParser = parser;
            eventHandler = parser.newEventHandler();
            LipidAdduct lipid = parser.parse(lipid_name, eventHandler, false);
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
                errorMessage = lipid_name;
            }
            message += ("at or after " + errorMessage);
        }
        throw new LipidParsingException("Could not parse lipid '" + lipid_name + " 'with any parser!" + message);
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
