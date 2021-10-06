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
