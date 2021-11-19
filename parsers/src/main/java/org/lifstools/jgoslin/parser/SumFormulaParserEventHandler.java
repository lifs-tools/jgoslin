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
