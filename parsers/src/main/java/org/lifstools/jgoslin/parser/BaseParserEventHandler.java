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

import java.lang.reflect.InvocationTargetException;
import org.lifstools.jgoslin.domain.LipidParsingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author dominik
 * @param <T>
 */
public abstract class BaseParserEventHandler<T> {
    public HashMap<String, Method> registered_events = new HashMap<>();
    public HashSet<String> rule_names = new HashSet<>();
    public Parser<T> parser = null;
    public String debug = "";
    public T content = null;

    public BaseParserEventHandler()
    {
        registered_events = new HashMap<>();
        rule_names = new HashSet<>();
    }


    // checking if all registered events are reasonable and orrur as rules in the grammar
    public void sanity_check()
    {
        for (String event_name : registered_events.keySet()) {
            if (!event_name.endsWith("_pre_event") && !event_name.endsWith("_post_event")){
                throw new RuntimeException("Parser event handler error: event '" + event_name + "' does not contain the suffix '_pre_event' or '_post_event'");
            }
            String rule_name = event_name.replace("_pre_event", "").replace("_post_event", "");
            if (!rule_names.contains(rule_name)){
                throw new RuntimeException("Parser event handler error: rule '" + rule_name + "' in event '" + event_name + "' is not present in the grammar" + (parser != null ? " '" + parser.grammar_name + "'" : ""));
            }
        }
    }


    public void handle_event(String event_name, TreeNode node)
    {
        if (debug.equals("full")){
            String reg_event = registered_events.containsKey(event_name) ? "*" : "";
            System.out.println(event_name + reg_event + ": \"" + node.get_text() + "\"");
        }

        if (registered_events.containsKey(event_name)){
            if (!debug.equals("") && !debug.equals("full")){
                System.out.println(event_name + ": \"" + node.get_text() + "\"");
            }
            try {
                registered_events.get(event_name).invoke(this, node);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                throw new LipidParsingException(e.toString(), e);
            }
        }
    }
}   
