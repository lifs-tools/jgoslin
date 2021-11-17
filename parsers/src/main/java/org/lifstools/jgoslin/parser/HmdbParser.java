/*
 * Copyright 2021 dominik.
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
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 *
 * @author dominik
 */
public class HmdbParser extends Parser<LipidAdduct> {

    private static final String DEFAULT_GRAMMAR_CONTENT = readGrammarContent("/HMDB.g4");

    private HmdbParser(String grammarContent, char quote) {
        super(grammarContent, quote);
    }

    public static HmdbParser newInstance(String grammarResourcePath, char quote) {
        return new HmdbParser(readGrammarContent(grammarResourcePath), quote);
    }

    public static HmdbParser newInstance() {
        return new HmdbParser(DEFAULT_GRAMMAR_CONTENT, StringFunctions.DEFAULT_QUOTE);
    }

    @Override
    public HmdbParserEventHandler newEventHandler() {
        return new HmdbParserEventHandler(KNOWN_FUNCTIONAL_GROUPS);
    }

}
