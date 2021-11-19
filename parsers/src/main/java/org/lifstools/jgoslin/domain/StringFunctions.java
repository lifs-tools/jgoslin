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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class StringFunctions {

    public static char DEFAULT_QUOTE = '\'';

    public static String strip(String s, char c) {
        if (s.length() > 0) {
            int st = 0;
            while (st < s.length() - 1 && s.charAt(st) == c) {
                ++st;
            }
            s = s.substring(st, s.length());
        }

        if (s.length() > 0) {
            int en = 0;
            while (en < s.length() - 1 && s.charAt(s.length() - 1 - en) == c) {
                ++en;
            }
            s = s.substring(0, s.length() - en);
        }
        return s;
    }

    public static ArrayList<String> splitString(String text) {
        return splitString(text, ',', DEFAULT_QUOTE, false);
    }

    public static ArrayList<String> splitString(String text, char _sep, char _quote) {
        return splitString(text, _sep, _quote, false);
    }

    public static ArrayList<String> splitString(String text, char separator, char _quote, boolean with_empty) {
        boolean in_quote = false;
        ArrayList<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        char last_char = '\0';
        boolean last_escaped_backslash = false;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            boolean escaped_backslash = false;
            if (!in_quote) {
                if (c == separator) {
                    String sb_string = sb.toString();
                    if (sb_string.length() > 0 || with_empty) {
                        tokens.add(sb_string);
                    }
                    sb = new StringBuilder();
                } else {
                    if (c == _quote) {
                        in_quote = !in_quote;
                    }
                    sb.append(c);
                }
            } else {
                if (c == '\\' && last_char == '\\' && !last_escaped_backslash) {
                    escaped_backslash = true;
                } else if (c == _quote && !(last_char == '\\' && !last_escaped_backslash)) {
                    in_quote = !in_quote;
                }
                sb.append(c);
            }

            last_escaped_backslash = escaped_backslash;
            last_char = c;
        }

        String sb_string_end = sb.toString();

        if (sb_string_end.length() > 0 || (last_char == ',' && with_empty)) {
            tokens.add(sb_string_end);
        }
        if (in_quote) {
            throw new ConstraintViolationException("Error: corrupted token in grammar: " + sb_string_end);
        }
        return tokens;
    }

    public static List<String> getResourceAsStringList(Resource resource) {
        ArrayList<String> lines;
        // read resource from classpath and current thread's context class loader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            lines = br.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            //always pass on the original exception
            throw new ConstraintViolationException("Error: Resource " + resource.getDescription() + " cannot be read.", e);
        }
        return lines;
    }

    public static List<String> getResourceAsStringList(String resourcePath) {
        return getResourceAsStringList(new ClassPathResource(resourcePath));
    }

    public static String getResourceAsString(Resource resource) {
        StringBuilder sb = new StringBuilder();
        // read resource from classpath and current thread's context class loader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            br.lines().forEach(line -> {
                sb.append(line).append("\n");
            });
            return sb.toString();
        } catch (IOException e) {
            //always pass on the original exception
            throw new ConstraintViolationException("Error: resource '" + resource.getDescription() + "' does not exist.", e);
        }
    }

    public static String getResourceAsString(String resourcePath) {
        return getResourceAsString(new ClassPathResource(resourcePath));
    }
}
