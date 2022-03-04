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

import org.lifstools.jgoslin.domain.LipidParsingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.lifstools.jgoslin.domain.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for parser event handling.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 * @param <T> the type of object created from parsers using this event handler.
 */
public abstract class BaseParserEventHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseParserEventHandler.class);

    protected Map<String, Consumer<TreeNode>> registeredEvents = new HashMap<>();
    protected Set<String> ruleNames = new HashSet<>();
    protected T content = null;
    protected String errorMessage = "";

    protected BaseParserEventHandler() {
        registeredEvents = new HashMap<>();
        ruleNames = new HashSet<>();
    }

    // checking if all registered events are reasonable and orrur as rules in the grammar
    protected void sanityCheck(Parser<T> parser) {
        for (String event_name : registeredEvents.keySet()) {
            if (!event_name.endsWith("_pre_event") && !event_name.endsWith("_post_event")) {
                throw new ConstraintViolationException("Parser event handler error: event '" + event_name + "' does not contain the suffix '_pre_event' or '_post_event'");
            }
            String rule_name = event_name.replace("_pre_event", "").replace("_post_event", "");
            if (!ruleNames.contains(rule_name)) {
                throw new ConstraintViolationException("Parser event handler error: rule '" + rule_name + "' in event '" + event_name + "' is not present in the grammar" + (parser != null ? " '" + parser.grammarName + "'" : ""));
            }
        }
    }

    protected void handleEvent(String event_name, TreeNode node) {
        if (log.isDebugEnabled()) {
            String reg_event = registeredEvents.containsKey(event_name) ? "*" : "";
            log.debug(event_name + reg_event + ": \"" + node.getText() + "\"");
        }

        if (registeredEvents.containsKey(event_name)) {
            if (log.isDebugEnabled()) {
                log.debug(event_name + ": \"" + node.getText() + "\"");
            }
            try {
                registeredEvents.get(event_name).accept(node);
            } catch (Exception e) {
                throw new LipidParsingException(e.toString(), e);
            }
        }
    }

    protected abstract void resetParser(TreeNode node);

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
