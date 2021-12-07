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

    private static final String DEFAULT_GRAMMAR = "SumFormula.g4";

    /**
     * Create a new instance of a {@link SumFormulaParser}.
     *
     * @param grammarContent the grammar text content
     * @param quote the quotation character used in the grammar
     */
    public SumFormulaParser(String grammarContent, char quote) {
        super(grammarContent, quote);
    }

    /**
     * Create a new instance of a {@link SumFormulaParser} with default grammar
     * {@link SumFormulaParser#DEFAULT_GRAMMAR} and default quote
     * {@link StringFunctions#DEFAULT_QUOTE}.
     */
    public SumFormulaParser() {
        this(StringFunctions.getResourceAsString(DEFAULT_GRAMMAR), StringFunctions.DEFAULT_QUOTE);
    }

    @Override
    public SumFormulaParserEventHandler newEventHandler() {
        return new SumFormulaParserEventHandler();
    }

}
