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
package org.lifstools.jgoslin.parser;

import java.util.Map;
import static java.util.Map.entry;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.Elements;
import org.lifstools.jgoslin.domain.ElementTable;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class SumFormulaParserEventHandler extends BaseParserEventHandler<ElementTable> {

    private Element element;
    private int count;

    public SumFormulaParserEventHandler() {
        try {
            registeredEvents = Map.ofEntries(
                entry("molecule_pre_event", this::resetParser),
                entry("element_group_post_event", this::elementGroupPostEvent),
                entry("element_pre_event", this::elementPreEvent),
                entry("single_element_pre_event", this::singleElementGroupPreEvent),
                entry("count_pre_event", this::countPreEvent)
            );
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize ShorthandParserEventHandler.");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = new ElementTable();
        element = Element.H;
        count = 0;
    }

    private void elementGroupPostEvent(TreeNode node) {
        content.put(element, content.get(element) + count);
    }

    private void elementPreEvent(TreeNode node) {

        String parsed_element = node.getText();

        if (Elements.ELEMENT_POSITIONS.containsKey(parsed_element)) {
            element = Elements.ELEMENT_POSITIONS.get(parsed_element);
        } else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }

    private void singleElementGroupPreEvent(TreeNode node) {
        String parsed_element = node.getText();
        if (Elements.ELEMENT_POSITIONS.containsKey(parsed_element)) {
            element = Elements.ELEMENT_POSITIONS.get(parsed_element);
            content.put(element, content.get(element) + 1);
        } else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }

    private void countPreEvent(TreeNode node) {
        count = Integer.valueOf(node.getText());
    }
}
