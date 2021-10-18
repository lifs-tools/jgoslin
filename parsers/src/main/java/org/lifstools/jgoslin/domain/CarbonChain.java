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

/**
 *
 * @author dominik
 */
public class CarbonChain extends FunctionalGroup {
    public CarbonChain(FattyAcid _fa){
        this(_fa, -1, 1);
    }
    
    
    public CarbonChain(FattyAcid _fa, int _position){
        this(_fa, _position, 1);
    }
    
    public CarbonChain(FattyAcid _fa, int _position, int _count){
        super("cc", _position, _count);
        if (_fa != null)
        {
            functional_groups.put("cc", new ArrayList<FunctionalGroup>());
            functional_groups.get("cc").add(_fa);
        }

        elements.put(Element.H, 1);
        elements.put(Element.O, -1);
    }


    @Override
    public FunctionalGroup copy()
    {
        return new CarbonChain((FattyAcid)functional_groups.get("cc").get(0).copy(), position, count);
    }


    @Override
    public String to_string(LipidLevel level)
    {
        return (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) ? Integer.toString(position) : "") + "(" + ((FattyAcid)functional_groups.get("cc").get(0)).to_string(level) + ")";
    }
}
