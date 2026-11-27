/*
 * Copyright 2026 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.parser;

import org.lifstools.jgoslin.domain.LipidAdduct;

/**
 * Event handler for the {@link ClassLevelParser}. The class level parser does
 * not use a grammar, this handler only holds the parse result and the error
 * message.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class ClassLevelParserEventHandler extends BaseParserEventHandler<LipidAdduct> {

    /**
     * Create a new {@code ClassLevelParserEventHandler}.
     */
    public ClassLevelParserEventHandler() {
        super();
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        errorMessage = "";
    }

}
