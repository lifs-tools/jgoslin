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

import org.lifstools.jgoslin.domain.StringFunctions;
import org.lifstools.jgoslin.domain.ElementTable;

/**
 * Parser implementation for chemical sum formulas.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class SumFormulaParser extends Parser<ElementTable> {

    private SumFormulaParser(String grammarContent, char quote) {
        super(grammarContent, quote);
    }

    /**
     * Create a new instance of a {@link SumFormulaParser}.
     *
     * @param grammarResourcePath the resource path to the grammar file
     * @param quote the quotation character used in the grammar
     * @return a new parser instance
     */
    public static SumFormulaParser newInstance(String grammarResourcePath, char quote) {
        return new SumFormulaParser(StringFunctions.getResourceAsString(grammarResourcePath), quote);
    }

    /**
     * Create a new instance of a {@link SumFormulaParser}.
     *
     * @return a new parser instance
     */
    public static SumFormulaParser newInstance() {
        return newInstance("SumFormula.g4", StringFunctions.DEFAULT_QUOTE);
    }

    @Override
    public SumFormulaParserEventHandler newEventHandler() {
        return new SumFormulaParserEventHandler();
    }

}
