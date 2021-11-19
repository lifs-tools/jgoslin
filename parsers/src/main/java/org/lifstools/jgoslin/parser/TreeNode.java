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
