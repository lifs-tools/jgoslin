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

import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class HeadgroupDecorator extends FunctionalGroup {
    public boolean suffix;
    public LipidLevel lowest_visible_level;
    
    public HeadgroupDecorator(String _name){
        this(_name, -1, 1, null, false, LipidLevel.NO_LEVEL);
    }

    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements){
        this(_name, _position, _count, _elements, false, LipidLevel.NO_LEVEL);
    }
    
    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements, boolean _suffix){
        this(_name, _position, _count, _elements, _suffix, LipidLevel.NO_LEVEL);
    }
    
    
    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements, boolean _suffix, LipidLevel _level){
        super(_name, _position, _count, null, false, "", _elements);
        suffix = _suffix;
        lowest_visible_level = _level;
    }

    @Override
    public FunctionalGroup copy(){
        ElementTable e = new ElementTable();
        for (Entry<Element, Integer> kv : elements.entrySet()){
            e.put(kv.getKey(), e.get(kv.getKey()) + kv.getValue());
        }
        return new HeadgroupDecorator(name, position, count, e, suffix, lowest_visible_level);
    }


    @Override
    public String to_string(LipidLevel level){
        if (!suffix) return name;
        String decorator_string = "";
        if ((lowest_visible_level == LipidLevel.NO_LEVEL) || (lowest_visible_level.level <= level.level)){

            if (functional_groups.containsKey("decorator_alkyl")){
                if (functional_groups.get("decorator_alkyl").size() > 0){
                    decorator_string = (level.level > LipidLevel.SPECIES.level) ? functional_groups.get("decorator_alkyl").get(0).to_string(level) : "Alk";
                }
                else {
                    decorator_string = "Alk";
                }
            }
            else if (functional_groups.containsKey("decorator_acyl")) {
                if (functional_groups.get("decorator_acyl").size() > 0){
                    decorator_string = (level.level > LipidLevel.SPECIES.level) ? ("FA " + functional_groups.get("decorator_acyl").get(0).to_string(level)) :
                    "FA";
                }
                else {
                    decorator_string = "FA";
                }
            }
            else
            {
                decorator_string = name;
            }
            decorator_string = "(" + decorator_string + ")";
        }

        return decorator_string;
    }



}
