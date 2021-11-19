/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.domain;

import java.util.Arrays;
import java.util.List;

/**
 * The lipid category nomenclature follows the shorthand notation of
 * <pre>Liebisch, G., Vizcaíno,
 * J.A., Köfeler, H., Trötzmüller, M., Griffiths, W.J., Schmitz, G., Spener, F.,
 * and Wakelam, M.J.O. (2013). Shorthand notation for lipid structures derived
 * from mass spectrometry. J. Lipid Res. 54, 1523–1530.</pre>
 *
 * We use the associations to either LipidMAPS or SwissLipids (Saccharolipids),
 * where appropriate.
 * 
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
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
        }).toList();
        if (matches.isEmpty()) {
            return LipidCategory.UNDEFINED;
        } else if (matches.size() > 1) {
            throw new ConstraintViolationException("Query string " + fullName + " found more than once in enum values! Please check enum definition: fullName is compared case insensitive!");
        }
        return matches.get(0);
    }
}
