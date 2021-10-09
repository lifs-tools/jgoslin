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
package com.lifs.jgoslin.domain;

import java.util.ArrayList;

/**
 *
 * @author dominik
 */
public class LipidSnPosition extends LipidMolecularSpecies {
    public LipidSnPosition(Headgroup _headgroup){
        this(_headgroup, null);
    }
    
    public LipidSnPosition(Headgroup _headgroup, ArrayList<FattyAcid> _fa){
        super(_headgroup, _fa);
        info.level = LipidLevel.SN_POSITION;
    }



    @Override
    public LipidLevel get_lipid_level()
    {
        return LipidLevel.SN_POSITION;
    }


    @Override
    public String get_lipid_string(){
        return get_lipid_string(LipidLevel.NO_LEVEL);
    }


    @Override
    public String get_lipid_string(LipidLevel level){
        switch(level){
            case NO_LEVEL:
            case SN_POSITION:
                return build_lipid_subspecies_name(LipidLevel.SN_POSITION);

            case MOLECULAR_SPECIES:
            case CATEGORY:
            case CLASS:
            case SPECIES:
                return super.get_lipid_string(level);

            default:
                throw new RuntimeException("LipidSnPosition does not know how to create a lipid string for level " + level.toString());
        }
    }

}
