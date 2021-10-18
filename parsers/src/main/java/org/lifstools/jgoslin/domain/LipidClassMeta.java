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

/**
 *
 * @author dominik
 */
public class LipidClassMeta{
    public LipidCategory lipid_category;
    public String class_name;
    public String description;
    public int max_num_fa;
    public int possible_num_fa;
    public HashSet<String> special_cases;
    public ElementTable elements;
    public ArrayList<String> synonyms;

    
    public LipidClassMeta(LipidCategory _lipid_category, String _class_name, String _description, int _max_num_fa, int _possible_num_fa, HashSet<String> _special_cases, ElementTable _elements, ArrayList<String> _synonyms){
        lipid_category = _lipid_category;
        class_name = _class_name;
        description = _description;
        max_num_fa = _max_num_fa;
        possible_num_fa = _possible_num_fa;
        special_cases = _special_cases;
        elements = _elements;
        synonyms = _synonyms;
    }
}
