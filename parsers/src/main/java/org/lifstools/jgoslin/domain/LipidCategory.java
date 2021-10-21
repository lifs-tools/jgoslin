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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dominik
 * @autho Nils Hoffmann
 */
public enum LipidCategory {

    NO_CATEGORY("Unknown lipid category"),
    UNDEFINED("Undefined lipid category"),
    /* SLM:000117142 Glycerolipids */
    GL("Glycerolipids"),
    /* SLM:000001193 Glycerophospholipids */
    GP("Glycerophospholipids"),
    /* SLM:000000525 Sphingolipids */
    SP("Sphingolipids"),
    /* SLM:000500463 Steroids and derivatives */
    ST("Sterollipids"),
    /* SLM:000390054 Fatty acyls and derivatives */
    FA("Fattyacyls"),
    /* LipidMAPS [PK]*/
    PK("Polyketides"),
    /* LipidMAPS [SL]*/
    SL("Saccharolipids");

    private final String fullName;

    private LipidCategory(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return this.fullName;
    }

    public static LipidCategory forFullName(String fullName) {
        List<LipidCategory> matches = Arrays.asList(LipidCategory.values()).stream().filter((t) -> {
            return t.getFullName().equalsIgnoreCase(fullName);
        }).collect(Collectors.toList());
        if (matches.isEmpty()) {
            return LipidCategory.UNDEFINED;
        } else if (matches.size() > 1) {
            throw new ConstraintViolationException("Query string " + fullName + " found more than once in enum values! Please check enum definition: fullName is compared case insensitive!");
        }
        return matches.get(0);
    }
}
