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

/**
 *
 * @author dominik
 */
public class LipidCompleteStructure extends LipidFullStructure {
    public LipidCompleteStructure(Headgroup _headgroup){
        this(_headgroup, null);
    }
    
    public LipidCompleteStructure(Headgroup _headgroup, ArrayList<FattyAcid> _fa){
        super(_headgroup, _fa);
        info.level = LipidLevel.FULL_STRUCTURE;
    }


    @Override
    public LipidLevel get_lipid_level(){
        return LipidLevel.FULL_STRUCTURE;
    }

    @Override
    public String get_lipid_string(){
        return get_lipid_string(LipidLevel.NO_LEVEL);
    }
    
    @Override
    public String get_lipid_string(LipidLevel level){
        switch(level){
            case NO_LEVEL:
            case COMPLETE_STRUCTURE:
                return super.build_lipid_subspecies_name(LipidLevel.COMPLETE_STRUCTURE);

            case FULL_STRUCTURE:
            case STRUCTURE_DEFINED:
            case SN_POSITION:
            case MOLECULAR_SPECIES:
            case SPECIES:
            case CATEGORY:
            case CLASS:
                return super.get_lipid_string(level);

            default:
                throw new IllegalArgumentException("LipidCompleteStructure does not know how to create a lipid string for level " + level.toString());
        }
    }

}
