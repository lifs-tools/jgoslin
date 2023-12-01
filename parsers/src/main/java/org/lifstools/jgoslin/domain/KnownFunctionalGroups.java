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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.lifstools.jgoslin.parser.BaseParserEventHandler;
import org.springframework.core.io.ClassPathResource;

/**
 * A lookup class that provides access to known functional groups defined in a
 * file following the format of functional-groups.csv.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class KnownFunctionalGroups extends HashMap<String, FunctionalGroup> {

    public static final int UNDEFINED_CLASS = 0;
    private final HashMap<String, ArrayList<Integer>> trivialMediators = new HashMap<>();

    private void loadData(List<String> lines, List<String> linesTM, SumFormulaParser sumFormulaParser) {
        int lineCounter = 0;
        ArrayList< ArrayList<String>> functional_data = new ArrayList<>();
        HashSet<String> functional_data_set = new HashSet<>();
        
        for (String line : linesTM) {
            if (line.isEmpty()) continue;
            
            ArrayList<String> tokens = StringFunctions.splitString(line, '\t', '"', true);
            if (tokens.size() != 2 || containsKey(tokens.get(0))) continue;
            
            ArrayList<Integer> positions = new ArrayList<>();
            for (String pos : StringFunctions.splitString(tokens.get(1), ',', '"', true)){
                positions.add(Integer.parseInt(pos));
            }
            trivialMediators.put(tokens.get(0), positions);
        }

        for (String line : lines) {
            if (lineCounter++ == 0) {
                continue;
            }
            ArrayList<String> tokens = StringFunctions.splitString(line, ',', '"', true);
            String fd_name = tokens.get(1);
            if (functional_data_set.contains(fd_name)) {
                throw new ConstraintViolationException("Error: functional group '" + fd_name + "' occurs multiple times in file!");
            }
            functional_data.add(tokens);
            functional_data_set.add(fd_name);
        }

        SumFormulaParser sfp = sumFormulaParser;
        BaseParserEventHandler<ElementTable> handler = sfp.newEventHandler();
        for (ArrayList<String> row : functional_data) {
            row.add(row.get(1));
            for (int i = 7; i < row.size(); ++i) {
                ElementTable et = row.get(2).length() > 0 ? sfp.parse(row.get(2), handler) : new ElementTable();
                if (row.get(0).equals("FG")) {
                    put(row.get(i), new FunctionalGroup(
                            row.get(1),
                            -1,
                            1,
                            new DoubleBonds(Integer.valueOf(row.get(3))),
                            (row.get(4).equals("1") ? true : false),
                            "",
                            (row.get(5).equals("1") ? true : false),
                            et,
                            this
                    ));
                } else {
                    put(row.get(i), new HeadgroupDecorator(
                            row.get(1),
                            -1,
                            1,
                            et,
                            this
                    ));
                }
            }
        }
    }

    public KnownFunctionalGroups() {
        this(StringFunctions.getResourceAsStringList(new ClassPathResource("functional-groups.csv")), new SumFormulaParser());
    }

    public KnownFunctionalGroups(List<String> lines, SumFormulaParser sumFormulaParser) {
        super();
        loadData(lines, StringFunctions.getResourceAsStringList(new ClassPathResource("trivial_mediators.csv")), sumFormulaParser);
    }

    public FunctionalGroup get(String s) {
        return super.get(s).copy();
    }

    public HashMap<String, ArrayList<Integer>> getTmDb() {
        return trivialMediators;
    }

    public ArrayList<Integer> db(String s) {
        return trivialMediators.get(s);
    }
}
