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

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class TreeNode {

    long rule_index;
    TreeNode left;
    TreeNode right;
    char terminal;
    boolean fire_event;
    public static final char EOF_SIGN = '\0';
    public static final String ONE_STR = "\0";

    TreeNode(long _rule, boolean _fire_event) {
        rule_index = _rule;
        left = null;
        right = null;
        terminal = '\0';
        fire_event = _fire_event;
    }

    String getText() {
        if (terminal == '\0') {
            String left_str = left.getText();
            String right_str = right != null ? right.getText() : "";
            return (!left_str.equals(ONE_STR) ? left_str : "") + (!right_str.equals(ONE_STR) ? right_str : "");
        }
        return String.valueOf(terminal);
    }

    int getInt() {
        return Integer.valueOf(getText());
    }
}
