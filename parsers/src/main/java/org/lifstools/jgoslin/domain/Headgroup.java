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

import com.fasterxml.jackson.annotation.JsonGetter;
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
 * association to {@link LipidClasses} and {@link LipidCategory} is maintained.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class Headgroup {
    
    public final static HashMap<String, LipidCategory> StringCategory = new HashMap<>();
    public final static HashMap<String, Integer> StringClass = new HashMap<>();
    public final static HashMap<Integer, String> ClassString = new HashMap<>();
    public final static HashSet<String> exceptionHeadgroups = new HashSet<>(Arrays.asList("Cer", "SPB"));
    private final static KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups();

    private String headgroup;
    private LipidCategory lipidCategory;
    private int lipidClass;
    private boolean useHeadgroup;
    private ArrayList<HeadgroupDecorator> decorators = new ArrayList<>();
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
    
    private static final Map<String, ArrayList<String>> GLYCO_TABLE = Map.ofEntries(
        entry("ga2", new ArrayList<String>(Arrays.asList("GalNAc", "Gal", "Glc"))),
        entry("gb3", new ArrayList<String>(Arrays.asList("Gal", "Gal", "Glc"))),
        entry("gb4", new ArrayList<String>(Arrays.asList("GalNAc", "Gal", "Gal", "Glc"))),
        entry("gd1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd1a", new ArrayList<String>(Arrays.asList("Hex", "Hex", "Hex", "HexNAc", "NeuAc", "NeuAc"))),
        entry("gd2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd3", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gm1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "Gal", "Glc"))),
        entry("gm2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "Gal", "Glc"))),
        entry("gm3", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "Glc"))),
        entry("gm4", new ArrayList<String>(Arrays.asList("NeuAc", "Gal"))),
        entry("gp1", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gq1", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt3", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd0a", new ArrayList<String>(Arrays.asList("HexNAc", "Hex", "NeuAc", "HexNAc", "Hex", "NeuAc", "Hex"))),
        entry("gd1b", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gq1b", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt1b", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd1a-ac", new ArrayList<String>(Arrays.asList("Hex", "Hex", "Hex", "HexNAc", "NeuAc", "NeuAc", "NeuAc"))),
        entry("gq1-ac", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt1b-ac", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt3-ac", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc")))
    );

    public Headgroup(String _headgroup) {
        this(_headgroup, null, false);
    }

    public Headgroup(String _headgroup, ArrayList<HeadgroupDecorator> _decorators, boolean _use_headgroup) {
        String hg = _headgroup.toLowerCase();
        if (GLYCO_TABLE.containsKey(hg) && !_use_headgroup){
            for (String carbohydrate : GLYCO_TABLE.get(hg)){
                FunctionalGroup functional_group = null;
                try {
                    functional_group = knownFunctionalGroups.get(carbohydrate);
                } catch (Exception e) {
                    throw new LipidParsingException("Carbohydrate '" + carbohydrate + "' unknown");
                }

                functional_group.getElements().put(Element.O, functional_group.getElements().get(Element.O) - 1);
                decorators.add((HeadgroupDecorator) functional_group);

            }
            _headgroup = "Cer";
        }
        
        headgroup = _headgroup;
        lipidCategory = getCategory(_headgroup);
        lipidClass = getClass(headgroup);
        useHeadgroup = _use_headgroup;
        if (_decorators != null){
            for (HeadgroupDecorator hgd : _decorators) decorators.add(hgd);
        }
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

    @JsonGetter("lipidClass")
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

        /*
        if (level == LipidLevel.CLASS) {
            return hgs;
        }
        */

        StringBuilder headgoup_string = new StringBuilder();

        // adding prefixes to the headgroup
        if (!LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)) {
            ArrayList<HeadgroupDecorator> decoratorsTmp = new ArrayList<>();
            for (HeadgroupDecorator hgd : decorators) {
                if (hgd.isSuffix()) continue;
                
                HeadgroupDecorator hgd_copy = (HeadgroupDecorator)hgd.copy();
                hgd_copy.name = hgd_copy.name.replace("Gal", "Hex");
                hgd_copy.name = hgd_copy.name.replace("Glc", "Hex");
                hgd_copy.name = hgd_copy.name.replace("S(3')", "S");
                decoratorsTmp.add(hgd_copy);
            }
            Collections.sort(decoratorsTmp);
            for (int i = decoratorsTmp.size() - 1; i > 0; --i){
                HeadgroupDecorator hge = decoratorsTmp.get(i);
                HeadgroupDecorator hgeBefore = decoratorsTmp.get(i - 1);
                if (hge.getName().equals(hgeBefore.getName())){
                    hgeBefore.setCount(hgeBefore.getCount() + hge.getCount());
                    decoratorsTmp.remove(i);
                }
            }
            for (HeadgroupDecorator hge : decoratorsTmp) {
                headgoup_string.append(hge.toString(level));
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
            elements.add(hgd.computeAndCopyElements(), hgd.count);
        });

        return elements;
    }

    @JsonGetter("name")
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

    @JsonIgnore
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
