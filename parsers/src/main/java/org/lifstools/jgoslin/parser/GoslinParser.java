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

import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.StringFunctions;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class GoslinParser extends Parser<LipidAdduct> {

    private static final String DEFAULT_GRAMMAR_CONTENT = "Goslin.g4";

    private final KnownFunctionalGroups knownFunctionalGroups;
    
    private GoslinParser(KnownFunctionalGroups knownFunctionalGroups, String grammarContent, char quote) {
        super(grammarContent, quote);
        this.knownFunctionalGroups = knownFunctionalGroups;
    }

    public static GoslinParser newInstance(KnownFunctionalGroups knownFunctionalGroups, String grammarResourcePath, char quote) {
        return new GoslinParser(knownFunctionalGroups, StringFunctions.getResourceAsString(grammarResourcePath), quote);
    }
    
    public static GoslinParser newInstance(KnownFunctionalGroups knownFunctionalGroups) {
        return newInstance(knownFunctionalGroups, DEFAULT_GRAMMAR_CONTENT, StringFunctions.DEFAULT_QUOTE);
    }    

    public static GoslinParser newInstance() {
        return newInstance(new KnownFunctionalGroups(), DEFAULT_GRAMMAR_CONTENT, StringFunctions.DEFAULT_QUOTE);
    }
    
    @Override
    public GoslinParserEventHandler newEventHandler() {
        return new GoslinParserEventHandler(knownFunctionalGroups);
    }

}
