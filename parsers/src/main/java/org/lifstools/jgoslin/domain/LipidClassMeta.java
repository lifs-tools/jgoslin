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
import java.util.HashSet;
import java.util.Objects;

/**
 *
 * @author dominik
 */
public final class LipidClassMeta {

    public final LipidCategory lipidCategory;
    public final String className;
    public final String description;
    public final int maxNumFa;
    public final int possibleNumFa;
    public final HashSet<String> specialCases;
    public final ElementTable elements;
    public final ArrayList<String> synonyms;

    public LipidClassMeta(LipidCategory _lipid_category, String _class_name, String _description, int _max_num_fa, int _possible_num_fa, HashSet<String> _special_cases, ElementTable _elements, ArrayList<String> _synonyms) {
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

    public HashSet<String> getSpecialCases() {
        return specialCases;
    }

    public ElementTable getElements() {
        return elements;
    }

    public ArrayList<String> getSynonyms() {
        return synonyms;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.lipidCategory);
        hash = 23 * hash + Objects.hashCode(this.className);
        hash = 23 * hash + Objects.hashCode(this.description);
        hash = 23 * hash + this.maxNumFa;
        hash = 23 * hash + this.possibleNumFa;
        hash = 23 * hash + Objects.hashCode(this.specialCases);
        hash = 23 * hash + Objects.hashCode(this.elements);
        hash = 23 * hash + Objects.hashCode(this.synonyms);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LipidClassMeta other = (LipidClassMeta) obj;
        if (this.maxNumFa != other.maxNumFa) {
            return false;
        }
        if (this.possibleNumFa != other.possibleNumFa) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.lipidCategory != other.lipidCategory) {
            return false;
        }
        if (!Objects.equals(this.specialCases, other.specialCases)) {
            return false;
        }
        if (!Objects.equals(this.elements, other.elements)) {
            return false;
        }
        if (!Objects.equals(this.synonyms, other.synonyms)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LipidClassMeta{" + "lipidCategory=" + lipidCategory + ", className=" + className + ", description=" + description + ", maxNumFa=" + maxNumFa + ", possibleNumFa=" + possibleNumFa + ", specialCases=" + specialCases + ", elements=" + elements + ", synonyms=" + synonyms + '}';
    }

}
