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
package org.lifstools.jgoslin.domain;

import org.lifstools.jgoslin.parser.SumFormulaParser;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import org.lifstools.jgoslin.parser.BaseParserEventHandler;
import org.springframework.core.io.ClassPathResource;

/**
 * Lookup class for lipid classes, consisting of {@link LipidClassMeta} entries,
 * derived from lipid-list.csv.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class LipidClasses extends ArrayList<LipidClassMeta> {

    private static final LipidClasses LIPID_CLASSES = new LipidClasses();
    public static final int UNDEFINED_CLASS = 0;

    private LipidClasses() {
        super();
        loadData(StringFunctions.getResourceAsStringList(new ClassPathResource("lipid-list.csv")), new SumFormulaParser());
    }

    private void loadData(List<String> lines, SumFormulaParser sfp) {
        add(new LipidClassMeta(LipidCategory.NO_CATEGORY,
                "UNDEFINED",
                "",
                0,
                0,
                new HashSet<>(),
                new ElementTable(),
                new ArrayList<>(Arrays.asList("UNDEFINED"))
        ));

        int lineCounter = 0;
        int SYNONYM_START_INDEX = 7;

        HashMap<String, ArrayList<String>> data = new HashMap<>();
        HashSet<String> keys = new HashSet<>();
        Map<String, Integer> enum_names = new HashMap<>(Map.ofEntries(
                entry("GL", 1),
                entry("GP", 1),
                entry("SP", 1),
                entry("ST", 1),
                entry("FA", 1),
                entry("PK", 1),
                entry("SL", 1),
                entry("UNDEFINED", 1)
        ));

        Map<String, LipidCategory> names_to_category = Map.ofEntries(
                entry("GL", LipidCategory.GL),
                entry("GP", LipidCategory.GP),
                entry("SP", LipidCategory.SP),
                entry("ST", LipidCategory.ST),
                entry("FA", LipidCategory.FA),
                entry("PK", LipidCategory.PK),
                entry("SL", LipidCategory.SL),
                entry("UNDEFINED", LipidCategory.UNDEFINED)
        );

        for (String line : lines) {
            if (lineCounter++ == 0) {
                continue;
            }
            ArrayList<String> tokens = StringFunctions.splitString(line, ',', '"', true);

            if (keys.contains(tokens.get(0))) {
                throw new ConstraintViolationException("Error: lipid name '" + tokens.get(0) + "' occurs multiple times in the lipid list.");
            }
            keys.add(tokens.get(0));

            for (int i = SYNONYM_START_INDEX; i < tokens.size(); ++i) {
                String test_lipid_name = tokens.get(i);
                if (test_lipid_name.length() == 0) {
                    continue;
                }
                if (keys.contains(test_lipid_name)) {
                    throw new ConstraintViolationException("Error: lipid name '" + test_lipid_name + "' occurs multiple times in the lipid list.");
                }
                keys.add(test_lipid_name);
            }

            String enum_name = tokens.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < enum_name.length(); ++i) {
                char c = enum_name.charAt(i);
                if ('A' <= c && c <= 'Z') {
                    sb.append(c);
                } else if ('0' <= c && c <= '9') {
                    sb.append(c);
                } else if ('a' <= c && c <= 'z') {
                    sb.append(Character.toString(c - ('a' - 'A')));
                } else {
                    sb.append('_');
                }
            }
            enum_name = sb.toString();

            if (enum_name.charAt(0) == '_') {
                enum_name = "L" + enum_name;
            }

            if (enum_name.charAt(0) < 'A' || 'Z' < enum_name.charAt(0)) {
                enum_name = "L" + enum_name;
            }

            if (!enum_names.containsKey(enum_name)) {
                enum_names.put(enum_name, 1);
            } else {
                int cnt = enum_names.get(enum_name) + 1;
                enum_names.put(enum_name, cnt);
                enum_name += ('A' + cnt - 1);
                enum_names.put(enum_name, 1);
            }

            data.put(enum_name, tokens);
        }

        // creating the lipid class dictionary
        BaseParserEventHandler<ElementTable> handler = sfp.newEventHandler();
        data.entrySet().forEach(kv -> {
            HashSet<String> special_cases = new HashSet<>();
            StringFunctions.splitString(kv.getValue().get(5), ';', '"').forEach(scase -> {
                special_cases.add(StringFunctions.strip(scase, '"'));
            });
            ElementTable e = kv.getValue().get(6).length() > 0 ? sfp.parse(kv.getValue().get(6), handler) : new ElementTable();
            ArrayList<String> synonyms = new ArrayList<>();
            synonyms.add(kv.getValue().get(0));
            for (int ii = SYNONYM_START_INDEX; ii < kv.getValue().size(); ++ii) {
                if (kv.getValue().get(ii).length() > 0) {
                    synonyms.add(StringFunctions.strip(kv.getValue().get(ii), '"'));
                }
            }
            add(new LipidClassMeta(names_to_category.get(kv.getValue().get(1)),
                    kv.getValue().get(0),
                    kv.getValue().get(2),
                    Integer.valueOf(kv.getValue().get(3)),
                    Integer.valueOf(kv.getValue().get(4)),
                    special_cases,
                    e,
                    synonyms));
        });
    }

    public static LipidClasses getInstance() {
        return LIPID_CLASSES;
    }
}
