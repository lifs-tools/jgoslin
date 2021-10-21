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

import java.util.Collection;

/**
 *
 * @author dominik
 */
public class LipidFullStructure extends LipidStructureDefined {

    public LipidFullStructure(Headgroup _headgroup, KnownFunctionalGroups knownFunctionalGroups) {
        this(_headgroup, null, knownFunctionalGroups);
    }

    public LipidFullStructure(Headgroup _headgroup, Collection<FattyAcid> _fa, KnownFunctionalGroups knownFunctionalGroups) {
        super(_headgroup, _fa, knownFunctionalGroups);
        info.level = LipidLevel.FULL_STRUCTURE;
    }

    @Override
    public LipidLevel getLipidLevel() {
        return LipidLevel.FULL_STRUCTURE;
    }

    @Override
    public String getLipidString() {
        return getLipidString(LipidLevel.NO_LEVEL);
    }

    @Override
    public String getLipidString(LipidLevel level) {
        switch (level) {
            case NO_LEVEL:
            case FULL_STRUCTURE:
                return super.buildLipidSubspeciesName(LipidLevel.FULL_STRUCTURE);

            case STRUCTURE_DEFINED:
            case SN_POSITION:
            case MOLECULAR_SPECIES:
            case SPECIES:
            case CATEGORY:
            case CLASS:
                return super.getLipidString(level);

            default:
                throw new IllegalArgumentException("LipidFullStructure does not know how to create a lipid string for level " + level.toString());
        }
    }

}
