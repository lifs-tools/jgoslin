/*
 * Copyright 2021 dominik.
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
package com.lifs.jgoslin.parser;

/**
 *
 * @author dominik
 */
public class TreeNode{
    public long rule_index;
    public TreeNode left;
    public TreeNode right;
    public char terminal;
    public boolean fire_event;
    public static final char EOF_SIGN = '\0';
    public static final String one_str = "\0";

    public TreeNode(long _rule, boolean _fire_event){
        rule_index = _rule;
        left = null;
        right = null;
        terminal = '\0';
        fire_event = _fire_event;
    }

    public String get_text(){
        if (terminal == '\0'){
            String left_str = left.get_text();
            String right_str = right != null ? right.get_text() : "";
            return (!left_str.equals(one_str) ? left_str : "") + (!right_str.equals(one_str) ? right_str : "");
        }
        return String.valueOf(terminal);
    }
}
