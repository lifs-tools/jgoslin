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

package org.lifstools.jgoslin.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author dominik
 */
public class Headgroup {
    public static HashMap<String, LipidCategory> StringCategory = new HashMap<>();
    public static HashMap<String, Integer> StringClass = new HashMap<>();
    public static HashMap<Integer, String> ClassString = new HashMap<>();
    public String headgroup;
    public LipidCategory lipid_category;
    public int lipid_class;
    public boolean use_headgroup;
    public ArrayList<HeadgroupDecorator> decorators;
    public boolean sp_exception;
    public static HashSet<String> exception_headgroups = new HashSet<>(Arrays.asList("Cer", "SPB"));

    public static final HashMap<LipidCategory, String> CategoryString = new HashMap<>(){{
        put(LipidCategory.NO_CATEGORY, "NO_CATEGORY");
        put(LipidCategory.UNDEFINED, "UNDEFINED");
        put(LipidCategory.GL, "GL");
        put(LipidCategory.GP, "GP");
        put(LipidCategory.SP, "SP");
        put(LipidCategory.ST, "ST");
        put(LipidCategory.FA, "FA");
        put(LipidCategory.SL, "SL");
    }};
    
    
    public Headgroup(String _headgroup){
        this(_headgroup, null, false);
    }

    public Headgroup(String _headgroup, ArrayList<HeadgroupDecorator> _decorators, boolean _use_headgroup){
        headgroup = _headgroup;
        lipid_category = get_category(_headgroup);
        lipid_class = get_class(headgroup);
        use_headgroup = _use_headgroup;
        decorators = (_decorators != null) ? _decorators : new ArrayList<>();
        sp_exception = (lipid_category == LipidCategory.SP) && exception_headgroups.contains(LipidClasses.get_instance().get(lipid_class).class_name) && (decorators.size() == 0);
    }


    public static LipidCategory get_category(String _headgroup){
        if (StringCategory.isEmpty()){
            
            for (LipidClassMeta lipid_class : LipidClasses.get_instance()){
                LipidCategory category = lipid_class.lipid_category;
                lipid_class.synonyms.forEach(hg -> {
                    StringCategory.put(hg, category);
                });
            }
        }

        return StringCategory.containsKey(_headgroup) ? StringCategory.get(_headgroup) : LipidCategory.UNDEFINED;
    }



    public static int get_class(String _headgroup){
        
        if (StringClass.isEmpty()){
            int l_class = 0;
            for (LipidClassMeta lipid_class : LipidClasses.get_instance()){
                for (String hg : lipid_class.synonyms){
                    StringClass.put(hg, l_class);
                }
                l_class += 1;
            }
        }

        return StringClass.containsKey(_headgroup) ? (int)StringClass.get(_headgroup) : LipidClasses.UNDEFINED_CLASS;
    }


    public static String get_class_string(int _lipid_class){
        if (ClassString.isEmpty()){
            int l_class = 0;
            for (LipidClassMeta lipid_class : LipidClasses.get_instance()){
                ClassString.put(l_class++, lipid_class.synonyms.get(0));
            }
        }

        return ClassString.containsKey(_lipid_class) ? ClassString.get(_lipid_class) : "UNDEFINED";
    }


    public String get_class_name(){
        return LipidClasses.get_instance().get(lipid_class).class_name;
    }


    public static String get_category_string(LipidCategory _lipid_category){
        return CategoryString.get(_lipid_category);
    }        


    public String get_lipid_string(LipidLevel level){
        if (level == LipidLevel.CATEGORY){
            return get_category_string(lipid_category);
        }

        String hgs = use_headgroup ? headgroup : get_class_string(lipid_class);

        if (level == LipidLevel.CLASS){
            return hgs;
        }

        StringBuilder headgoup_string = new StringBuilder();

        // adding prefixes to the headgroup
        if (!LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)){
            ArrayList<String> prefixes = new ArrayList<>();
            for (HeadgroupDecorator hgd : decorators){
                if (!hgd.suffix) prefixes.add(hgd.to_string(level));
            }
            Collections.sort(prefixes);
            for (String prefix : prefixes) headgoup_string.append(prefix);
        }
        else {
            for (HeadgroupDecorator hgd : decorators) {
                if (!hgd.suffix) headgoup_string.append(hgd.to_string(level)).append("-");
            }
        }

        // adding headgroup
        headgoup_string.append(hgs);

        // ading suffixes to the headgroup
        for (HeadgroupDecorator hgd : decorators){
            if (hgd.suffix) headgoup_string.append(hgd.to_string(level));
        }
        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) && lipid_category == LipidCategory.SP && !sp_exception){
            headgoup_string.append("(1)");
        }

        return headgoup_string.toString();
    }


    public ElementTable get_elements(){

        if (use_headgroup || LipidClasses.get_instance().size() <= lipid_class)
            throw new RuntimeException("Element table cannot be computed for lipid '" + headgroup + "'");

        ElementTable elements = LipidClasses.get_instance().get(lipid_class).elements.copy();
        decorators.forEach(hgd -> {
            elements.add(hgd.get_elements());
        });

        return elements;
    }
}