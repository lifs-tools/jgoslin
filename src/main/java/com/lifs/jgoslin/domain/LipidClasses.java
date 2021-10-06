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

package com.lifs.jgoslin.domain;

import com.lifs.jgoslin.parser.SumFormulaParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author dominik
 */
public class LipidClasses extends ArrayList<LipidClassMeta> {
    private static LipidClasses lipid_classes = null;
    public static final int UNDEFINED_CLASS = 0;
    
    private LipidClasses() {
        List<String> lines;
        add(null);
        
        try {
            lines = Files.readAllLines(Path.of("src/main/antlr4/lipid-list.csv"));
        }
        catch(Exception e){
            throw new RuntimeException("File lipid-list.csv cannot be read.");
        }
        int lineCounter = 0;
        int SYNONYM_START_INDEX = 7;


        HashMap<String, ArrayList<String> > data = new HashMap<>();
        HashSet<String> keys = new HashSet<>();
        ArrayList<String> list_keys = new ArrayList<>();
        HashMap<String, Integer> enum_names = new HashMap<String, Integer>(){{
            put("GL", 1);
            put("GP", 1);
            put("SP", 1);
            put("ST", 1);
            put("FA", 1);
            put("PK", 1);
            put("SL", 1);
            put("UNDEFINED", 1);
        }};
        
        
        HashMap<String, LipidCategory> names_to_category = new HashMap<>(){{
            put("GL", LipidCategory.GL);
            put("GP", LipidCategory.GP);
            put("SP", LipidCategory.SP);
            put("ST", LipidCategory.ST);
            put("FA", LipidCategory.FA);
            put("PK", LipidCategory.PK);
            put("SL", LipidCategory.SL);
            put("UNDEFINED", LipidCategory.UNDEFINED);
        }};


        for (String line : lines){
            if (lineCounter++ == 0) continue;
            ArrayList<String> tokens = StringFunctions.split_string(line, ',', '"', true);
            
            if (keys.contains(tokens.get(0))){
                throw new RuntimeException("Error: lipid name '" + tokens.get(0) + "' occurs multiple times in the lipid list.");
            }
            keys.add(tokens.get(0));

            for (int i = SYNONYM_START_INDEX; i < tokens.size(); ++i){
                String test_lipid_name = tokens.get(i);
                if (test_lipid_name.length() == 0) continue;
                if (keys.contains(test_lipid_name)){
                    throw new RuntimeException("Error: lipid name '" + test_lipid_name + "' occurs multiple times in the lipid list.");
                }
                keys.add(test_lipid_name);
            }

            String enum_name = tokens.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < enum_name.length(); ++i){
                char c = enum_name.charAt(i);
                if ('A' <= c && c <= 'Z'){
                    sb.append(c);
                }
                else if ('0' <= c && c <= '9'){
                    sb.append(c);
                }
                else if ('a' <= c && c <= 'z'){
                    sb.append(Character.toString(c - ('a' - 'A')));
                }
                else
                {
                    sb.append('_');
                }
            }
            enum_name = sb.toString();


            if (enum_name.charAt(0) == '_'){
                enum_name = "L" + enum_name;
            }

            if (enum_name.charAt(0) < 'A' || 'Z' < enum_name.charAt(0)){
                enum_name = "L" + enum_name;
            }

            if (!enum_names.containsKey(enum_name)){
                enum_names.put(enum_name, 1);
            }
            else
            {
                int cnt = enum_names.get(enum_name) + 1;
                enum_names.put(enum_name, cnt);
                enum_name += ('A' + cnt - 1);
                enum_names.put(enum_name, 1);
            }

            data.put(enum_name, tokens);
            list_keys.add(enum_name);
        }
        
        // creating the lipid class dictionary
        SumFormulaParser sfp = new SumFormulaParser();
        data.entrySet().forEach(kv -> {
            HashSet<String> special_cases = new HashSet<>();
            StringFunctions.split_string(kv.getValue().get(5), ',', '"').forEach(scase -> {
                special_cases.add(scase.strip());
            });
            ElementTable e = sfp.parse(kv.getValue().get(6));
            ArrayList<String> synonyms = new ArrayList<>();
            synonyms.add(kv.getValue().get(0)); 
            for (int ii = SYNONYM_START_INDEX; ii < kv.getValue().size(); ++ii) synonyms.add(kv.getValue().get(ii));
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
            
            
    public static LipidClasses get_instance(){
        if (lipid_classes == null) lipid_classes = new LipidClasses();
        return lipid_classes;
    }
}
