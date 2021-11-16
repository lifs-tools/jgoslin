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
    public LipidCategory lipidCategory;
    public int lipidClass;
    public boolean useHeadgroup;
    public ArrayList<HeadgroupDecorator> decorators;
    public boolean spException;
    public static HashSet<String> exceptionHeadgroups = new HashSet<>(Arrays.asList("Cer", "SPB"));

    public static final HashMap<LipidCategory, String> CategoryString = new HashMap<>() {
        {
            put(LipidCategory.NO_CATEGORY, "NO_CATEGORY");
            put(LipidCategory.UNDEFINED, "UNDEFINED");
            put(LipidCategory.GL, "GL");
            put(LipidCategory.GP, "GP");
            put(LipidCategory.SP, "SP");
            put(LipidCategory.ST, "ST");
            put(LipidCategory.FA, "FA");
            put(LipidCategory.SL, "SL");
        }
    };

    public Headgroup(String _headgroup) {
        this(_headgroup, null, false);
    }

    public Headgroup(String _headgroup, ArrayList<HeadgroupDecorator> _decorators, boolean _use_headgroup) {
        headgroup = _headgroup;
        lipidCategory = getCategory(_headgroup);
        lipidClass = getClass(headgroup);
        useHeadgroup = _use_headgroup;
        decorators = (_decorators != null) ? _decorators : new ArrayList<>();
        spException = (lipidCategory == LipidCategory.SP) && exceptionHeadgroups.contains(LipidClasses.getInstance().get(lipidClass).className) && (decorators.size() == 0);
    }

    public static LipidCategory getCategory(String _headgroup) {
        if (StringCategory.isEmpty()) {

            for (LipidClassMeta lipid_class : LipidClasses.getInstance()) {
                LipidCategory category = lipid_class.lipidCategory;
                lipid_class.synonyms.forEach(hg -> {
                    StringCategory.put(hg, category);
                });
            }
        }

        return StringCategory.containsKey(_headgroup) ? StringCategory.get(_headgroup) : LipidCategory.UNDEFINED;
    }

    public static int getClass(String _headgroup) {

        if (StringClass.isEmpty()) {
            int l_class = 0;
            for (LipidClassMeta lipid_class : LipidClasses.getInstance()) {
                for (String hg : lipid_class.synonyms) {
                    StringClass.put(hg, l_class);
                }
                l_class += 1;
            }
        }

        return StringClass.containsKey(_headgroup) ? (int) StringClass.get(_headgroup) : LipidClasses.UNDEFINED_CLASS;
    }

    public static String getClassString(int _lipid_class) {
        if (ClassString.isEmpty()) {
            int l_class = 0;
            for (LipidClassMeta lipid_class : LipidClasses.getInstance()) {
                ClassString.put(l_class++, lipid_class.synonyms.get(0));
            }
        }

        return ClassString.containsKey(_lipid_class) ? ClassString.get(_lipid_class) : "UNDEFINED";
    }

    public String getClassName() {
        return LipidClasses.getInstance().get(lipidClass).className;
    }

    public static String getCategoryString(LipidCategory _lipid_category) {
        return CategoryString.get(_lipid_category);
    }

    public String getLipidString(LipidLevel level) {
        if (level == LipidLevel.CATEGORY) {
            return getCategoryString(lipidCategory);
        }

        String hgs = useHeadgroup ? headgroup : getClassString(lipidClass);

        if (level == LipidLevel.CLASS) {
            return hgs;
        }

        StringBuilder headgoup_string = new StringBuilder();

        // adding prefixes to the headgroup
        if (!LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)) {
            ArrayList<String> prefixes = new ArrayList<>();
            for (HeadgroupDecorator hgd : decorators) {
                if (!hgd.suffix) {
                    prefixes.add(hgd.toString(level));
                }
            }
            Collections.sort(prefixes);
            for (String prefix : prefixes) {
                headgoup_string.append(prefix);
            }
        } else {
            for (HeadgroupDecorator hgd : decorators) {
                if (!hgd.suffix) {
                    headgoup_string.append(hgd.toString(level)).append("-");
                }
            }
        }

        // adding headgroup
        headgoup_string.append(hgs);

        // ading suffixes to the headgroup
        for (HeadgroupDecorator hgd : decorators) {
            if (hgd.suffix) {
                headgoup_string.append(hgd.toString(level));
            }
        }
        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) && lipidCategory == LipidCategory.SP && !spException) {
            headgoup_string.append("(1)");
        }

        return headgoup_string.toString();
    }

    public ElementTable getElements() {

        if (useHeadgroup || LipidClasses.getInstance().size() <= lipidClass) {
            throw new RuntimeException("Element table cannot be computed for lipid '" + headgroup + "'");
        }

        ElementTable elements = LipidClasses.getInstance().get(lipidClass).elements.copy();
        decorators.forEach(hgd -> {
            elements.add(hgd.computeAndCopyElements());
        });

        return elements;
    }
}
