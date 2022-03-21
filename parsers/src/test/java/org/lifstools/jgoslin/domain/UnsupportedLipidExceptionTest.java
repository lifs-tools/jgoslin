/*
 * Copyright 2022 Nils Hoffmann.
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Nils Hoffmann
 */
public class UnsupportedLipidExceptionTest {

    @Test
    public void testException() {
        UnsupportedLipidException ule = new UnsupportedLipidException();
        assertEquals(null, ule.getMessage());
        ule = new UnsupportedLipidException("Test message");
        assertEquals("Test message", ule.getMessage());
        ule = new UnsupportedLipidException(new Throwable("Test throwable"));
        assertEquals("java.lang.Throwable: Test throwable", ule.getMessage());
        ule = new UnsupportedLipidException("Test message", new Throwable("Test throwable"));
        assertEquals("Test message", ule.getMessage());
    }
}
