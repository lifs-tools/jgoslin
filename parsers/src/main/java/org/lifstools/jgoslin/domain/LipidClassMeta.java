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

import java.util.List;
import java.util.Set;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class LipidClassMeta {

    public final LipidCategory lipidCategory;
    public final String className;
    public final String description;
    public final int maxNumFa;
    public final int possibleNumFa;
    public final Set<String> specialCases;
    public final ElementTable elements;
    public final List<String> synonyms;

    public LipidClassMeta(LipidCategory _lipid_category, String _class_name, String _description, int _max_num_fa, int _possible_num_fa, Set<String> _special_cases, ElementTable _elements, List<String> _synonyms) {
        lipidCategory = _lipid_category;
        className = _class_name;
        description = _description;
        maxNumFa = _max_num_fa;
        possibleNumFa = _possible_num_fa;
        specialCases = _special_cases;
        elements = _elements;
        synonyms = _synonyms;
    }

    public LipidCategory getLipidCategory() {
        return lipidCategory;
    }

    public String getClassName() {
        return className;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxNumFa() {
        return maxNumFa;
    }

    public int getPossibleNumFa() {
        return possibleNumFa;
    }

    public Set<String> getSpecialCases() {
        return specialCases;
    }

    public ElementTable getElements() {
        return elements;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

}
