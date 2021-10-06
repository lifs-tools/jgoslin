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
package com.lifs.jgoslin.domain;

import com.lifs.jgoslin.parser.SumFormulaParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author dominik
 */
public class KnownFunctionalGroups extends HashMap<String, FunctionalGroup> {
    private static KnownFunctionalGroups known_functional_groups = null;
    
    private KnownFunctionalGroups(){
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of("src/main/antlr4/functional-groups.csv"));
        }
        catch(Exception e){
            throw new RuntimeException("File lipid-list.csv cannot be read.");
        }
        int lineCounter = 0;
        ArrayList< ArrayList<String> > functional_data = new ArrayList<>();
        HashSet<String> functional_data_set = new HashSet<>();
        
        
        for (String line : lines){
            if (lineCounter++ == 0) continue;
            ArrayList<String> tokens = StringFunctions.split_string(line, ',', '"', true);
            String fd_name = tokens.get(1);
            if (functional_data_set.contains(fd_name))
            {
                throw new RuntimeException("Error: functional group '" + fd_name + "' occurs multiple times in file!");
            }
            functional_data.add(tokens);
            functional_data_set.add(fd_name);
        }
        
        SumFormulaParser sfp = new SumFormulaParser();
        for (ArrayList<String> row : functional_data){
            row.add(row.get(1));
            for (int i = 6; i < row.size(); ++i){
                ElementTable et = row.get(2).length() > 0 ? sfp.parse(row.get(2)) : new ElementTable();
                if (row.get(0).equals("FG")){
                    put(row.get(i), new FunctionalGroup(
                        row.get(1),
                        -1,
                        1,
                        new DoubleBonds(Integer.valueOf(row.get(3))),
                        (row.get(4).equals("1") ? true : false),
                        "",
                        et
                    ));
                }
                else {
                    put(row.get(i), new HeadgroupDecorator(
                        row.get(1),
                        -1,
                        1,
                        et
                    ));
                }
            }
        }
    }
    
    public static KnownFunctionalGroups get_instance(){
        if (known_functional_groups == null) known_functional_groups = new KnownFunctionalGroups();
        return known_functional_groups;
    }
}
