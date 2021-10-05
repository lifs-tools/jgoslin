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
package com.lifs.jgoslin.domain;

import java.util.ArrayList;

/**
 *
 * @author dominik
 */
public class StringFunctions {
    public static char DEFAULT_QUOTE = '\'';
    public static String compute_sum_formula(ElementTable elements){
        StringBuilder ss = new StringBuilder();

        for (Elements.Element e : Elements.element_order){
            if (elements.get(e) > 0) ss.append(Elements.element_shortcut.get(e));
            if (elements.get(e) > 1) ss.append(elements.get(e));
        }
        return ss.toString();
    }


    public static ArrayList<String> split_string(String text){
        return split_string(text, ',', DEFAULT_QUOTE, false);
    }
    

    public static ArrayList<String> split_string(String text, char separator, char _quote, boolean with_empty){
        boolean in_quote = false;
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char last_char = '\0';
        boolean last_escaped_backslash = false;

        for (int i = 0; i < text.length(); ++i){
            char c = text.charAt(i);
            boolean escaped_backslash = false;
            if (!in_quote)
            {
                if (c == separator)
                {
                    String sb_string = sb.toString();
                    if (sb_string.length() > 0 || with_empty) tokens.add(sb_string);
                    sb = new StringBuilder();
                }
                else
                {
                    if (c == _quote) in_quote = !in_quote;
                    sb.append(c);
                }
            }
            else 
            {
                if (c == '\\' && last_char == '\\' && !last_escaped_backslash)
                {
                    escaped_backslash = true;
                }
                else if (c == _quote && !(last_char == '\\' && !last_escaped_backslash))
                {
                    in_quote = !in_quote;
                }
                sb.append(c);
            }

            last_escaped_backslash = escaped_backslash;
            last_char = c;
        }

        String sb_string_end = sb.toString();

        if (sb_string_end.length() > 0 || (last_char == ',' && with_empty))
        {
            tokens.add(sb_string_end);
        }
        if (in_quote){
            throw new RuntimeException("Error: corrupted token in grammar");
        }
        return tokens;
    }
}
