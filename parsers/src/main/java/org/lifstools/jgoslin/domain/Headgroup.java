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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Map.entry;

/**
 * This class represents functional head groups of lipids. This is where the
 * association to {@link LipidClass} and {@link LipidCategory} is maintained.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class Headgroup {

    public static HashMap<String, LipidCategory> StringCategory = new HashMap<>();
    public static HashMap<String, Integer> StringClass = new HashMap<>();
    public static HashMap<Integer, String> ClassString = new HashMap<>();
    public static HashSet<String> exceptionHeadgroups = new HashSet<>(Arrays.asList("Cer", "SPB"));

    private String headgroup;
    private LipidCategory lipidCategory;
    private int lipidClass;
    private boolean useHeadgroup;
    private ArrayList<HeadgroupDecorator> decorators;
    private boolean spException;

    public static final Map<LipidCategory, String> CategoryString = Map.ofEntries(
            entry(LipidCategory.NO_CATEGORY, "NO_CATEGORY"),
            entry(LipidCategory.UNDEFINED, "UNDEFINED"),
            entry(LipidCategory.GL, "GL"),
            entry(LipidCategory.GP, "GP"),
            entry(LipidCategory.SP, "SP"),
            entry(LipidCategory.ST, "ST"),
            entry(LipidCategory.FA, "FA"),
            entry(LipidCategory.SL, "SL")
    );

    public Headgroup(String _headgroup) {
        this(_headgroup, null, false);
    }

    public Headgroup(String _headgroup, ArrayList<HeadgroupDecorator> _decorators, boolean _use_headgroup) {
        headgroup = _headgroup;
        lipidCategory = getCategory(_headgroup);
        lipidClass = getClass(headgroup);
        useHeadgroup = _use_headgroup;
        decorators = (_decorators != null) ? _decorators : new ArrayList<>();
        spException = (lipidCategory == LipidCategory.SP) && exceptionHeadgroups.contains(LipidClasses.getInstance().get(lipidClass).lipidClassName) && (decorators.isEmpty());
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
        return LipidClasses.getInstance().get(lipidClass).lipidClassName;
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
                if (!hgd.isSuffix()) {
                    prefixes.add(hgd.toString(level));
                }
            }
            Collections.sort(prefixes);
            for (String prefix : prefixes) {
                headgoup_string.append(prefix);
            }
        } else {
            for (HeadgroupDecorator hgd : decorators) {
                if (!hgd.isSuffix()) {
                    headgoup_string.append(hgd.toString(level)).append("-");
                }
            }
        }

        // adding headgroup
        headgoup_string.append(hgs);

        // ading suffixes to the headgroup
        for (HeadgroupDecorator hgd : decorators) {
            if (hgd.isSuffix()) {
                headgoup_string.append(hgd.toString(level));
            }
        }
        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) && lipidCategory == LipidCategory.SP && !spException) {
            headgoup_string.append("(1)");
        }

        return headgoup_string.toString();
    }

    @JsonIgnore
    public ElementTable getElements() {

        if (useHeadgroup || LipidClasses.getInstance().size() <= lipidClass) {
            throw new ConstraintViolationException("Element table cannot be computed for lipid '" + headgroup + "'");
        }

        ElementTable elements = LipidClasses.getInstance().get(lipidClass).elements.copy();
        decorators.forEach(hgd -> {
            elements.add(hgd.computeAndCopyElements());
        });

        return elements;
    }

    public String getHeadgroup() {
        return headgroup;
    }

    public void setHeadgroup(String headgroup) {
        this.headgroup = headgroup;
    }

    public LipidCategory getLipidCategory() {
        return lipidCategory;
    }

    public void setLipidCategory(LipidCategory lipidCategory) {
        this.lipidCategory = lipidCategory;
    }

    public int getLipidClass() {
        return lipidClass;
    }

    public void setLipidClass(int lipidClass) {
        this.lipidClass = lipidClass;
    }

    @JsonIgnore
    public boolean isUseHeadgroup() {
        return useHeadgroup;
    }

    public void setUseHeadgroup(boolean useHeadgroup) {
        this.useHeadgroup = useHeadgroup;
    }

    @JsonIgnore
    public boolean isSpException() {
        return spException;
    }

    public void setSpException(boolean spException) {
        this.spException = spException;
    }

    @JsonIgnore
    public ArrayList<HeadgroupDecorator> getDecorators() {
        return decorators;
    }

    public void setDecorators(ArrayList<HeadgroupDecorator> decorators) {
        this.decorators = decorators;
    }

}
