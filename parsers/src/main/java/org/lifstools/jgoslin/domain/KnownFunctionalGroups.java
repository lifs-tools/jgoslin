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

import org.lifstools.jgoslin.parser.SumFormulaParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.lifstools.jgoslin.parser.SumFormulaParserEventHandler;

/**
 *
 * @author dominik
 */
public final class KnownFunctionalGroups extends HashMap<String, FunctionalGroup> {

    public KnownFunctionalGroups(String resourceName, SumFormulaParser sumFormulaParser) {
        ArrayList<String> lines = new ArrayList<>();

        // read resource from classpath and current thread's context class loader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)))) {
            lines = br.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            //always pass on the original exception
            throw new RuntimeException("Error: Resource " + resourceName + " cannot be read.", e);
        }

        int lineCounter = 0;
        ArrayList< ArrayList<String>> functional_data = new ArrayList<>();
        HashSet<String> functional_data_set = new HashSet<>();

        for (String line : lines) {
            if (lineCounter++ == 0) {
                continue;
            }
            ArrayList<String> tokens = StringFunctions.splitString(line, ',', '"', true);
            String fd_name = tokens.get(1);
            if (functional_data_set.contains(fd_name)) {
                throw new RuntimeException("Error: functional group '" + fd_name + "' occurs multiple times in file!");
            }
            functional_data.add(tokens);
            functional_data_set.add(fd_name);
        }

        SumFormulaParser sfp = sumFormulaParser;
        SumFormulaParserEventHandler handler = sfp.newEventHandler();
        for (ArrayList<String> row : functional_data) {
            row.add(row.get(1));
            for (int i = 6; i < row.size(); ++i) {
                ElementTable et = row.get(2).length() > 0 ? sfp.parse(row.get(2), handler) : new ElementTable();
                if (row.get(0).equals("FG")) {
                    put(row.get(i), new FunctionalGroup(
                            row.get(1),
                            -1,
                            1,
                            new DoubleBonds(Integer.valueOf(row.get(3))),
                            (row.get(4).equals("1") ? true : false),
                            "",
                            et,
                            this
                    ));
                } else {
                    put(row.get(i), new HeadgroupDecorator(
                            row.get(1),
                            -1,
                            1,
                            et,
                            this
                    ));
                }
            }
        }
    }

    public KnownFunctionalGroups() {
        this("/functional-groups.csv", SumFormulaParser.newInstance());
    }

    public FunctionalGroup get(String s) {
        return super.get(s).copy();
    }
}
