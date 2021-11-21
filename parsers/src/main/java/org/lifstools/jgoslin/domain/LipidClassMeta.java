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
 * Metadata information about a lipid on class level.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 * @see LipidClasses
 * @see LipidLevel
 */
public final class LipidClassMeta {

    public final LipidCategory lipidCategory;
    public final String lipidClassName;
    public final String description;
    public final int maxNumFa;
    public final int possibleNumFa;
    public final Set<String> specialCases;
    public final ElementTable elements;
    public final List<String> synonyms;

    /**
     * Create a new lipid class meta instance.
     *
     * @param lipidCategory the lipid category
     * @param lipidClassName the lipid class name
     * @param description the description
     * @param maxNumFa the maximum number of fatty acids allowed for this lipid
     * class
     * @param possibleNumFa the minimum possible number of fatty acids for this
     * lipid class
     * @param specialCases the special cases
     * @param elements the elements (sum formula) of this class head group
     * @param synonyms the synonyms of this lipid class head group
     */
    public LipidClassMeta(LipidCategory lipidCategory, String lipidClassName, String description, int maxNumFa, int possibleNumFa, Set<String> specialCases, ElementTable elements, List<String> synonyms) {
        this.lipidCategory = lipidCategory;
        this.lipidClassName = lipidClassName;
        this.description = description;
        this.maxNumFa = maxNumFa;
        this.possibleNumFa = possibleNumFa;
        this.specialCases = specialCases;
        this.elements = elements;
        this.synonyms = synonyms;
    }

    public LipidCategory getLipidCategory() {
        return lipidCategory;
    }

    public String getClassName() {
        return lipidClassName;
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
