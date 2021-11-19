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
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class CarbonChain extends FunctionalGroup {

    public CarbonChain(FattyAcid _fa, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, -1, 1, knownFunctionalGroups);
    }

    public CarbonChain(FattyAcid _fa, int _position, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, _position, 1, knownFunctionalGroups);
    }

    public CarbonChain(FattyAcid _fa, int _position, int _count, KnownFunctionalGroups knownFunctionalGroups) {
        super("cc", _position, _count, knownFunctionalGroups);
        if (_fa != null) {
            functionalGroups.put("cc", new ArrayList<>());
            functionalGroups.get("cc").add(_fa);
        }

        elements.put(Element.H, 1);
        elements.put(Element.O, -1);
    }

    @Override
    public FunctionalGroup copy() {
        return new CarbonChain((FattyAcid) functionalGroups.get("cc").get(0).copy(), getPosition(), getCount(), knownFunctionalGroups);
    }

    @Override
    public String toString(LipidLevel level) {
        return (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) ? Integer.toString(getPosition()) : "") + "(" + ((FattyAcid) functionalGroups.get("cc").get(0)).toString(level) + ")";
    }
    
}
