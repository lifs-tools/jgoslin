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

import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.StringFunctions;
import org.lifstools.jgoslin.domain.LipidAdduct;

/**
 * Parser implementation for the LIPID MAPS lipid shorthand nomenclature.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LipidMapsParser extends Parser<LipidAdduct> {

    private static final String DEFAULT_GRAMMAR = "LipidMaps.g4";

    private final KnownFunctionalGroups knownFunctionalGroups;
    
    /**
     * Create a new instance of a {@link LipidMapsParser}.
     *
     * @param knownFunctionalGroups the known functional groups
     * @param grammarContent the grammar text content
     * @param quote the quotation character used in the grammar
     */
    public LipidMapsParser(KnownFunctionalGroups knownFunctionalGroups, String grammarContent, char quote) {
        super(grammarContent, quote);
        this.knownFunctionalGroups = knownFunctionalGroups;
    }

    /**
     * Create a new instance of a {@link LipidMapsParser} with default grammar
     * {@link LipidMapsParser#DEFAULT_GRAMMAR} and default quote
     * {@link StringFunctions#DEFAULT_QUOTE}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public LipidMapsParser(KnownFunctionalGroups knownFunctionalGroups) {
        this(knownFunctionalGroups, StringFunctions.getResourceAsString(DEFAULT_GRAMMAR), StringFunctions.DEFAULT_QUOTE);
    }

    /**
     * Create a new instance of a {@link LipidMapsParser} with default grammar
     * {@link LipidMapsParser#DEFAULT_GRAMMAR} and default quote
     * {@link StringFunctions#DEFAULT_QUOTE} and default
     * {@link KnownFunctionalGroups}.
     */
    public LipidMapsParser() {
        this(new KnownFunctionalGroups(), StringFunctions.getResourceAsString(DEFAULT_GRAMMAR), StringFunctions.DEFAULT_QUOTE);
    }

    @Override
    public LipidMapsParserEventHandler newEventHandler() {
        return new LipidMapsParserEventHandler(knownFunctionalGroups);
    }

}
