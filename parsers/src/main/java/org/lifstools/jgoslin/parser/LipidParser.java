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
import org.lifstools.jgoslin.domain.LipidParsingException;

/**
 *
 * @author dominik
 */
public class LipidParser {

    private final List<Parser<LipidAdduct>> parserList;
    private Parser<LipidAdduct> lastSuccessfulParser = null;

    public LipidParser(Parser<LipidAdduct>... parsers) {
        parserList = Arrays.asList(parsers);
    }

    public LipidParser() {
        this(
                ShorthandParser.newInstance(),
                FattyAcidParser.newInstance(),
                GoslinParser.newInstance(),
                LipidMapsParser.newInstance(),
                SwissLipidsParser.newInstance(),
                HmdbParser.newInstance()
        );
    }

    /**
     * This method tries multiple parsers in a defined order to parse the
     * provided lipid name. If no parser is able to parse the name successfully,
     * an exception is thrown.
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
        throw new LipidParsingException("Could not parse lipid '" + lipid_name + "'with any parser!" + ((lastParser != null) ? " Last message was: " + ((eventHandler == null) ? "unknown" : eventHandler.errorMessage) : ""));
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
