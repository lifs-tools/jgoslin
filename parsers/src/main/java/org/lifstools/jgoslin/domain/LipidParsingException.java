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

/**
 * Specific exception that is thrown when errors are encountered in a lipid
 * name.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class LipidParsingException extends LipidException {

    protected LipidParsingException() {
        super();
    }

    public LipidParsingException(String s) {
        super(s);
    }

    public LipidParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LipidParsingException(Throwable cause) {
        super(cause);
    }
}
