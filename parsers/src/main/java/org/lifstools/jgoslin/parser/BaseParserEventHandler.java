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

import org.lifstools.jgoslin.domain.LipidParsingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 *
 * @author dominik
 * @param <T>
 */
public abstract class BaseParserEventHandler<T> {

    protected HashMap<String, Consumer<TreeNode>> registeredEvents = new HashMap<>();
    protected HashSet<String> ruleNames = new HashSet<>();
    protected String debug = "";
    protected T content = null;
    protected String errorMessage = "";

    public BaseParserEventHandler() {
        registeredEvents = new HashMap<>();
        ruleNames = new HashSet<>();
    }

    // checking if all registered events are reasonable and orrur as rules in the grammar
    void sanityCheck(Parser<T> parser) {
        for (String event_name : registeredEvents.keySet()) {
            if (!event_name.endsWith("_pre_event") && !event_name.endsWith("_post_event")) {
                throw new RuntimeException("Parser event handler error: event '" + event_name + "' does not contain the suffix '_pre_event' or '_post_event'");
            }
            String rule_name = event_name.replace("_pre_event", "").replace("_post_event", "");
            if (!ruleNames.contains(rule_name)) {
                throw new RuntimeException("Parser event handler error: rule '" + rule_name + "' in event '" + event_name + "' is not present in the grammar" + (parser != null ? " '" + parser.grammarName + "'" : ""));
            }
        }
    }

    void handleEvent(String event_name, TreeNode node) {
        if (debug.equals("full")) {
            String reg_event = registeredEvents.containsKey(event_name) ? "*" : "";
            System.out.println(event_name + reg_event + ": \"" + node.getText() + "\"");
        }

        if (registeredEvents.containsKey(event_name)) {
            if (!debug.equals("") && !debug.equals("full")) {
                System.out.println(event_name + ": \"" + node.getText() + "\"");
            }
            try {
                registeredEvents.get(event_name).accept(node);
            } catch (Exception e) {
                throw new LipidParsingException(e.toString(), e);
            }
        }
    }
    
    abstract void resetParser(TreeNode node);
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
