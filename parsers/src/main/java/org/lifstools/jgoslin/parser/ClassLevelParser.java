/*
 * Copyright 2026 Dominik Kopczynski, Nils Hoffmann.
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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidCategory;
import org.lifstools.jgoslin.domain.LipidClassMeta;
import org.lifstools.jgoslin.domain.LipidClasses;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.LipidSpecies;
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 * Parser for names that consist of a lipid class or a lipid category only,
 * e.g. 'PC', 'TG', 'HexCer', 'CE' (lipid class level) or 'GP', 'SP' (lipid
 * category level). Class names and their synonyms come from lipid-list.csv
 * (see {@link LipidClasses}), category names from {@link LipidCategory}. A
 * category name that is also the primary name of a class (e.g. 'FA', 'SL') is
 * read as that class; a category name that is only a class synonym (e.g.
 * 'ST', a synonym of 'ST 27:1;O') is read as the category. Names are matched
 * exactly and case-sensitive, after removing leading and trailing spaces.
 *
 * Names with fatty acyl information are not handled here, they are parsed by
 * the grammar based parsers. The parsed lipid is a {@link LipidSpecies} on
 * level {@link LipidLevel#CLASS} or {@link LipidLevel#CATEGORY}, its element
 * table, sum formula and mass can not be computed.
 *
 * This parser does not use a grammar. It is the last parser of the
 * {@link LipidParser}, so that the grammar based parsers take precedence.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class ClassLevelParser extends Parser<LipidAdduct> {

    private static final String GRAMMAR_NAME = "ClassLevel";

    private static final Map<String, LipidCategory> CATEGORIES = Arrays.stream(LipidCategory.values())
            .filter(c -> c != LipidCategory.UNDEFINED && c != LipidCategory.NO_CATEGORY)
            .collect(Collectors.toUnmodifiableMap(LipidCategory::name, Function.identity()));

    private final KnownFunctionalGroups knownFunctionalGroups;

    /**
     * Create a new instance of a {@link ClassLevelParser}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public ClassLevelParser(KnownFunctionalGroups knownFunctionalGroups) {
        super();
        this.grammarName = GRAMMAR_NAME;
        this.knownFunctionalGroups = knownFunctionalGroups;
    }

    /**
     * Create a new instance of a {@link ClassLevelParser} with default
     * {@link KnownFunctionalGroups}.
     */
    public ClassLevelParser() {
        this(new KnownFunctionalGroups());
    }

    @Override
    public ClassLevelParserEventHandler newEventHandler() {
        return new ClassLevelParserEventHandler();
    }

    /**
     * Parse a lipid class or lipid category name.
     *
     * @param textToParse the lipid class or category name.
     * @param parserEventHandler the event handler that receives the result.
     * @param throwError if true, throws a {@link LipidParsingException} if the
     * name is neither a lipid class nor a lipid category.
     * @return the {@link LipidAdduct} on class or category level, or
     * {@code null}, if parsing failed and throwError is {@code false}.
     */
    @Override
    public LipidAdduct parse(String textToParse, BaseParserEventHandler<LipidAdduct> parserEventHandler, boolean throwError) {
        parserEventHandler.content = null;
        parserEventHandler.errorMessage = "";

        String name = StringFunctions.strip(textToParse, ' ');
        int lipidClass = Headgroup.getClass(name);
        LipidClassMeta classMeta = LipidClasses.getInstance().get(lipidClass);
        boolean isCategory = CATEGORIES.containsKey(name);
        boolean isClass = lipidClass != LipidClasses.UNDEFINED_CLASS
                && classMeta.lipidCategory != LipidCategory.UNDEFINED
                && classMeta.lipidCategory != LipidCategory.NO_CATEGORY;

        Headgroup headgroup;
        LipidLevel level;
        if (isClass && (!isCategory || classMeta.lipidClassName.equals(name))) {
            headgroup = new Headgroup(name);
            level = LipidLevel.CLASS;
        } else if (isCategory) {
            headgroup = new Headgroup(name);
            headgroup.setLipidClass(LipidClasses.UNDEFINED_CLASS);
            headgroup.setLipidCategory(CATEGORIES.get(name));
            headgroup.setSpException(false);
            level = LipidLevel.CATEGORY;
        } else {
            String message = "Lipid '" + name + "' is neither a lipid class nor a lipid category";
            if (throwError) {
                throw new LipidParsingException(message);
            }
            parserEventHandler.errorMessage = message;
            return null;
        }

        LipidSpecies lipid = new LipidSpecies(headgroup, knownFunctionalGroups);
        lipid.getInfo().setLevel(level);
        parserEventHandler.content = new LipidAdduct(lipid, null);
        return parserEventHandler.content;
    }

}
