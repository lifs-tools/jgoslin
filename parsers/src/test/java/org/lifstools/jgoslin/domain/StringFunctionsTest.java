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

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Nils Hoffmann
 */
public class StringFunctionsTest {

    @Test
    public void testGetResourceAsStringListThrows() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> {
            StringFunctions.getResourceAsStringList("jklasd.csv");
        });
        assertEquals("Error: Resource 'class path resource [jklasd.csv]' does not exist.", ex.getMessage());
    }

    @Test
    public void testGetResourceAsStringThrows() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> {
            StringFunctions.getResourceAsString("jklasd.csv");
        });
        assertEquals("Error: Resource 'class path resource [jklasd.csv]' does not exist.", ex.getMessage());
    }

    @Test
    public void testSplitString() {
        String testString = "a,b,c,\'quo ted\',d";
        ArrayList<String> al = StringFunctions.splitString(testString);
        assertEquals(5, al.size());
        assertEquals("a", al.get(0));
        assertEquals("b", al.get(1));
        assertEquals("c", al.get(2));
        assertEquals("'quo ted'", al.get(3));
        assertEquals("d", al.get(4));
    }
    
    @Test
    public void testSplitStringArgs() {
        String testString = "a,b,c,\'quo ted\',d";
        ArrayList<String> al = StringFunctions.splitString(testString, ',', '\'');
        assertEquals(5, al.size());
        assertEquals("a", al.get(0));
        assertEquals("b", al.get(1));
        assertEquals("c", al.get(2));
        assertEquals("'quo ted'", al.get(3));
        assertEquals("d", al.get(4));
    }
    
    @Test
    public void testSplitStringEscapedBackslash() {
        String testString = "a,b,c,\'quo ted\',\\d";
        ArrayList<String> al = StringFunctions.splitString(testString);
        assertEquals(5, al.size());
        assertEquals("a", al.get(0));
        assertEquals("b", al.get(1));
        assertEquals("c", al.get(2));
        assertEquals("'quo ted'", al.get(3));
        assertEquals("\\d", al.get(4));
    }
    
    @Test
    public void testSplitStringTrailingComma() {
        String testString = "a,b,c,\'quo ted\',\\d,";
        ArrayList<String> al = StringFunctions.splitString(testString);
        assertEquals(5, al.size());
        assertEquals("a", al.get(0));
        assertEquals("b", al.get(1));
        assertEquals("c", al.get(2));
        assertEquals("'quo ted'", al.get(3));
        assertEquals("\\d", al.get(4));
        al = StringFunctions.splitString(testString, ',', '\'', true);
        assertEquals(6, al.size());
        assertEquals("a", al.get(0));
        assertEquals("b", al.get(1));
        assertEquals("c", al.get(2));
        assertEquals("'quo ted'", al.get(3));
        assertEquals("\\d", al.get(4));
        assertEquals("", al.get(5));
    }
    
    @Test
    public void testSplitStringCorrupt() {
        String testString = "a,b,c,\'quo ted,\\d,";
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, ()->{
            StringFunctions.splitString(testString);
        });
        assertEquals("Error: corrupt token in grammar: " + "''quo ted,\\d,'", ex.getMessage());
        
    }
}
