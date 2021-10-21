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
import org.lifstools.jgoslin.domain.StringFunctions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class Parser<T> {

    // DP stands for dynamic programming
    final class DPNode {

        public long rule_index_1;
        public long rule_index_2;
        public DPNode left = null;
        public DPNode right = null;

        public DPNode(long _rule1, long _rule2, DPNode _left, DPNode _right) {
            rule_index_1 = _rule1;
            rule_index_2 = _rule2;
            left = _left;
            right = _right;
        }
    }

    enum Context {
        NoContext, InLineComment, InLongComment, InQuote
    };

    enum MatchWords {
        NoMatch, LineCommentStart, LineCommentEnd, LongCommentStart, LongCommentEnd, Quote
    };

    protected static final int SHIFT = 32;
    protected static final long MASK = (1L << SHIFT) - 1;
    protected static final char RULE_ASSIGNMENT = ':';
    protected static final char RULE_SEPARATOR = '|';
    protected static final char RULE_TERMINAL = ';';
    protected static final char EOF_SIGN = (char) 1;
    protected static final long EOF_RULE = 1L;
    protected static final long START_RULE = 2L;
    protected static final String EOF_RULE_NAME = "EOF";

    protected long nextFreeRuleIndex;
    protected HashMap<Character, HashSet<Long>> TtoNT = new HashMap<>();
    protected HashMap<Character, Long> originalTtoNT = new HashMap<>();
    protected HashMap<Long, HashSet<Long>> NTtoNT = new HashMap<>();
    protected HashMap<Long, String> NTtoRule = new HashMap<>();
    protected HashMap<Long, ArrayList<Long>> substitution = new HashMap<>();
    protected ArrayList<Bitfield> rightPair = new ArrayList<>();
    protected int avgPair;
    protected char quote;
    protected BaseParserEventHandler<T> parserEventHandler = null;
    protected boolean wordInGrammar = false;
    protected String grammarName = "";
    protected boolean usedEof = false;
    protected static final char DEFAULT_QUOTE = '\'';
    protected String errorMessage = "";

    public Parser(BaseParserEventHandler<T> _parserEventHandler, String grammarContent) {
        this(_parserEventHandler, grammarContent, (char) '\0');
    }

    public Parser(BaseParserEventHandler<T> _parserEventHandler, String grammarContent, char _quote) {
        this.quote = (_quote != 0) ? _quote : DEFAULT_QUOTE;
        this.parserEventHandler = _parserEventHandler;
        readGrammar(grammarContent);
    }

    long get_next_free_rule_index() {
        if (nextFreeRuleIndex <= MASK) {
            return nextFreeRuleIndex++;
        }
        throw new RuntimeException("Error: grammar is too big.");
    }

    final void readGrammar(String grammar) {
        nextFreeRuleIndex = START_RULE;
        wordInGrammar = false;
        grammarName = "";
        usedEof = false;
        HashMap<String, Long> ruleToNT = new HashMap<>();

        // interpret the rules and create the structure for parsing
        ArrayList<String> rules = extract_text_based_rules(grammar, quote);
        ArrayList<String> tokens = StringFunctions.splitString(rules.get(0), ' ', quote);
        grammarName = tokens.get(1);

        rules.remove(0);
        ruleToNT.put(EOF_RULE_NAME, EOF_RULE);
        TtoNT.put(EOF_SIGN, new HashSet<>());
        TtoNT.get(EOF_SIGN).add(EOF_RULE);

        for (String rule_line : rules) {
            ArrayList<String> tokens_level_1 = new ArrayList<>();
            ArrayList<String> line_tokens = StringFunctions.splitString(rule_line, RULE_ASSIGNMENT, quote);
            for (String t : line_tokens) {
                tokens_level_1.add(StringFunctions.strip(t, ' '));
            }

            if (tokens_level_1.size() != 2) {
                throw new RuntimeException("Error: corrupted token in grammar rule: '" + rule_line + "'");
            }

            ArrayList<String> rule_tokens = StringFunctions.splitString(tokens_level_1.get(0), ' ', quote);
            if (rule_tokens.size() > 1) {
                throw new RuntimeException("Error: several rule names on left hand side in grammar rule: '" + rule_line + "'");
            }
            String rule = tokens_level_1.get(0);

            if (rule.equals(EOF_RULE_NAME)) {
                throw new RuntimeException("Error: rule name is not allowed to be called EOF");
            }

            ArrayList<String> products = StringFunctions.splitString(tokens_level_1.get(1), RULE_SEPARATOR, quote);
            for (int i = 0; i < products.size(); ++i) {
                products.set(i, StringFunctions.strip(products.get(i), ' '));
            }

            if (!ruleToNT.containsKey(rule)) {
                ruleToNT.put(rule, get_next_free_rule_index());
            }
            long new_rule_index = ruleToNT.get(rule);

            if (!NTtoRule.containsKey(new_rule_index)) {
                NTtoRule.put(new_rule_index, rule);
            }

            for (String product : products) {
                ArrayList<String> non_terminals = new ArrayList<>();
                ArrayDeque<Long> non_terminal_rules = new ArrayDeque<>();
                ArrayList<String> product_rules = StringFunctions.splitString(product, ' ', quote);
                for (String NT : product_rules) {
                    String stripedNT = StringFunctions.strip(NT, ' ');
                    if (is_terminal(stripedNT, quote)) {
                        stripedNT = de_escape(stripedNT, quote);
                    }
                    non_terminals.add(stripedNT);
                    usedEof |= (stripedNT.equals(EOF_RULE_NAME));
                }

                String NTFirst = non_terminals.get(0);
                if (non_terminals.size() > 1 || !is_terminal(NTFirst, quote) || NTFirst.length() != 3) {
                    for (String non_terminal : non_terminals) {
                        if (is_terminal(non_terminal, quote)) {
                            non_terminal_rules.add(add_terminal(non_terminal));
                        } else {
                            if (!ruleToNT.containsKey(non_terminal)) {
                                ruleToNT.put(non_terminal, get_next_free_rule_index());
                            }
                            non_terminal_rules.add(ruleToNT.get(non_terminal));
                        }
                    }
                } else {
                    char c = NTFirst.charAt(1);
                    long tRule = 0;
                    if (!TtoNT.containsKey(c)) {
                        tRule = get_next_free_rule_index();
                        TtoNT.put(c, new HashSet<>());
                        TtoNT.get(c).add(tRule);

                    } else {
                        tRule = (new ArrayList<>(TtoNT.get(c))).get(0);
                    }

                    if (!NTtoNT.containsKey(tRule)) {
                        NTtoNT.put(tRule, new HashSet<>());
                    }
                    NTtoNT.get(tRule).add(new_rule_index);
                }

                // more than two rules, insert intermediate rule indexes
                while (non_terminal_rules.size() > 2) {
                    long rule_index_2 = non_terminal_rules.pollLast();
                    long rule_index_1 = non_terminal_rules.pollLast();

                    long key = compute_rule_key(rule_index_1, rule_index_2);
                    long next_index = get_next_free_rule_index();
                    if (!NTtoNT.containsKey(key)) {
                        NTtoNT.put(key, new HashSet<>());
                    }
                    NTtoNT.get(key).add(next_index);
                    non_terminal_rules.add(next_index);
                }

                // two product rules
                if (non_terminal_rules.size() == 2) {
                    long rule_index_2 = non_terminal_rules.pollLast();
                    long rule_index_1 = non_terminal_rules.pollLast();
                    long key = compute_rule_key(rule_index_1, rule_index_2);
                    if (!NTtoNT.containsKey(key)) {
                        NTtoNT.put(key, new HashSet<>());
                    }
                    NTtoNT.get(key).add(new_rule_index);
                } // only one product rule
                else if (non_terminal_rules.size() == 1) {
                    long rule_index_1 = non_terminal_rules.pollLast();
                    if (rule_index_1 == new_rule_index) {
                        throw new RuntimeException("Error: corrupted token in grammar: rule '" + rule + "' is not allowed to refer soleley to itself.");
                    }

                    if (!NTtoNT.containsKey(rule_index_1)) {
                        NTtoNT.put(rule_index_1, new HashSet<>());
                    }
                    NTtoNT.get(rule_index_1).add(new_rule_index);
                }
            }
        }

        // adding all rule names into the event handler
        for (Entry<String, Long> rule_name : ruleToNT.entrySet()) {
            parserEventHandler.ruleNames.add(rule_name.getKey());
        }

        parserEventHandler.sanityCheck();

        // keeping the original terminal dictionary
        for (Entry<Character, HashSet<Long>> kv : TtoNT.entrySet()) {
            for (long rule : kv.getValue()) {
                originalTtoNT.put(kv.getKey(), rule);
                break;
            }
        }

        // creating substitution dictionary for adding single rule chains into the parsing tree
        HashSet<Long> visited = new HashSet<>();
        for (Entry<Long, HashSet<Long>> kv : NTtoNT.entrySet()) {
            HashSet<Long> values = new HashSet<Long>();
            values.add(kv.getKey());
            for (long rule : values) {
                if (visited.contains(rule)) {
                    continue;
                }
                visited.add(rule);

                ArrayList<Long> topnodes = collect_one_backwards(rule);
                for (long rule_top : topnodes) {
                    ArrayList< ArrayList<Long>> chains = collect_backwards(rule, rule_top);

                    for (ArrayList<Long> cchain : chains) {
                        ArrayList<Long> chain = cchain;
                        while (chain.size() > 1) {
                            long top = chain.get(0);
                            chain.remove(0);
                            long key = kv.getKey() + (top << 16);
                            if (!substitution.containsKey(key)) {
                                substitution.put(key, chain);

                                if (chain.size() > 1) {
                                    ArrayList<Long> new_chain = new ArrayList<>();
                                    for (long e : chain) {
                                        new_chain.add(e);
                                    }
                                    chain = new_chain;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // expanding terminal dictionary for single rule chains
        HashSet<Character> keys = new HashSet<>();
        for (Entry<Character, HashSet<Long>> key : TtoNT.entrySet()) {
            keys.add(key.getKey());
        }
        for (char c : keys) {
            HashSet<Long> k_rules = new HashSet<>();
            for (long rule : TtoNT.get(c)) {
                k_rules.add(rule);
            }

            for (long rule : k_rules) {
                ArrayList<Long> backward_rules = collect_one_backwards(rule);
                for (long p : backward_rules) {
                    TtoNT.get(c).add(p);
                }
            }
        }

        // expanding non-terminal dictionary for single rule chains
        HashSet<Long> keysNT = new HashSet<>();
        for (Entry<Long, HashSet<Long>> k : NTtoNT.entrySet()) {
            keysNT.add(k.getKey());
        }
        for (long r : keysNT) {
            HashSet<Long> k_rules = new HashSet<>();
            for (long rr : NTtoNT.get(r)) {
                k_rules.add(rr);
            }

            for (long rule : k_rules) {
                ArrayList<Long> backward_rules = collect_one_backwards(rule);
                for (long p : backward_rules) {
                    NTtoNT.get(r).add(p);
                }
            }
        }

        // creating lookup table for right index pairs to a given left index
        for (long i = 0; i < nextFreeRuleIndex; ++i) {
            rightPair.add(new Bitfield((int) nextFreeRuleIndex));
        }

        for (Entry<Long, HashSet<Long>> kv : NTtoNT.entrySet()) {
            if (kv.getKey() <= MASK) {
                continue;
            }
            rightPair.get((int) ((kv.getKey() >>> SHIFT))).add((int) (kv.getKey() & MASK));
        }
    }

    ArrayList<String> extract_text_based_rules(String grammar, char _quote) {
        ArrayList<String> rules = null;
        int grammar_length = grammar.length();

        /*
        deleting comments to prepare for splitting the grammar in rules.
        Therefore, we have to consider three different contexts, namely
        within a quote, within a line comment, within a long comment.
        As long as we are in one context, key words for starting / ending
        the other contexts have to be ignored.
         */
        StringBuilder sb = new StringBuilder();
        Context current_context = Context.NoContext;
        int current_position = 0;
        int last_escaped_backslash = -1;

        for (int i = 0; i < grammar_length - 1; ++i) {
            MatchWords match = MatchWords.NoMatch;

            if (i > 0 && grammar.charAt(i) == '\\' && grammar.charAt(i - 1) == '\\' && last_escaped_backslash != i - 1) {
                last_escaped_backslash = i;
                continue;
            }

            if (grammar.charAt(i) == '/' && grammar.charAt(i + 1) == '/') {
                match = MatchWords.LineCommentStart;
            } else if (grammar.charAt(i) == '\n') {
                match = MatchWords.LineCommentEnd;
            } else if (grammar.charAt(i) == '/' && grammar.charAt(i + 1) == '*') {
                match = MatchWords.LongCommentStart;
            } else if (grammar.charAt(i) == '*' && grammar.charAt(i + 1) == '/') {
                match = MatchWords.LongCommentEnd;
            } else if (grammar.charAt(i) == _quote && !(i >= 1 && grammar.charAt(i - 1) == '\\' && i - 1 != last_escaped_backslash)) {
                match = MatchWords.Quote;
            }

            if (match != MatchWords.NoMatch) {
                switch (current_context) {
                    case NoContext:
                        switch (match) {
                            case LongCommentStart:
                                sb.append(grammar.substring(current_position, i));
                                current_context = Context.InLongComment;
                                break;

                            case LineCommentStart:
                                sb.append(grammar.substring(current_position, i));
                                current_context = Context.InLineComment;
                                break;

                            case Quote:
                                current_context = Context.InQuote;
                                break;

                            default:
                                break;
                        }
                        break;

                    case InQuote:
                        if (match == MatchWords.Quote) {
                            current_context = Context.NoContext;
                        }
                        break;

                    case InLineComment:
                        if (match == MatchWords.LineCommentEnd) {
                            current_context = Context.NoContext;
                            current_position = i + 1;
                        }
                        break;

                    case InLongComment:
                        if (match == MatchWords.LongCommentEnd) {
                            current_context = Context.NoContext;
                            current_position = i + 2;
                        }
                        break;

                    default:
                        break;
                }
            }
        }

        if (current_context == Context.NoContext) {
            sb.append(grammar.substring(current_position, grammar_length));
        } else {
            throw new RuntimeException("Error: corrupted grammar, ends either in comment or quote");
        }

        grammar = sb.toString();
        grammar = grammar.replace("\r\n", "");
        grammar = grammar.replace("\n", "");
        grammar = grammar.replace("\r", "");
        grammar = StringFunctions.strip(grammar, ' ');

        if (grammar.charAt(grammar.length() - 1) != RULE_TERMINAL) {
            throw new RuntimeException("Error: corrupted grammar, last rule has no termininating sign, was: '" + grammar.substring(grammar.length() - 1) + "'");
        }

        rules = StringFunctions.splitString(grammar, RULE_TERMINAL, _quote);

        if (rules.size() < 1) {
            throw new RuntimeException("Error: corrupted grammar, grammar is empty");
        }
        ArrayList<String> grammar_name_rule = StringFunctions.splitString(rules.get(0), ' ', _quote);

        if (grammar_name_rule.size() > 0 && !grammar_name_rule.get(0).equals("grammar")) {
            throw new RuntimeException("Error: first rule must start with the keyword 'grammar'");
        } else if (grammar_name_rule.size() != 2) {
            throw new RuntimeException("Error: incorrect first rule");
        }

        return rules;
    }

    long compute_rule_key(long rule_index_1, long rule_index_2) {
        return (rule_index_1 << SHIFT) | rule_index_2;
    }

    // checking if string is terminal
    boolean is_terminal(String product_token, char _quote) {
        return product_token.charAt(0) == _quote && product_token.charAt(product_token.length() - 1) == _quote && product_token.length() > 2;
    }

    String de_escape(String text, char _quote) {
        // remove the escape chars
        StringBuilder sb = new StringBuilder();
        boolean last_escape_char = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            boolean escape_char = false;

            if (c != '\\') {
                sb.append(c);
            } else {
                if (!last_escape_char) {
                    escape_char = true;
                } else {
                    sb.append(c);
                }
            }

            last_escape_char = escape_char;

        }
        return sb.toString();
    }

    // splitting the whole terminal in a tree structure where characters of terminal are the leafs and the inner nodes are added non terminal rules
    long add_terminal(String text) {
        ArrayDeque<Long> terminal_rules = new ArrayDeque<>();
        for (int i = 1; i < text.length() - 1; ++i) {
            char c = text.charAt(i);
            long tRule = 0;
            if (!TtoNT.containsKey(c)) {
                tRule = get_next_free_rule_index();
                TtoNT.put(c, new HashSet<>());
                TtoNT.get(c).add(tRule);
            } else {
                tRule = (new ArrayList<>(TtoNT.get(c))).get(0);
            }
            terminal_rules.add(tRule);
        }

        while (terminal_rules.size() > 1) {
            long rule_index_2 = terminal_rules.pollLast();
            long rule_index_1 = terminal_rules.pollLast();
            long next_index = get_next_free_rule_index();

            long key = compute_rule_key(rule_index_1, rule_index_2);
            if (!NTtoNT.containsKey(key)) {
                NTtoNT.put(key, new HashSet<>());
            }
            NTtoNT.get(key).add(next_index);
            terminal_rules.add(next_index);
        }
        return terminal_rules.pollLast();
    }

    ArrayList<Long> top_nodes(long rule_index) {
        ArrayList<Long> collection = new ArrayList<>();
        ArrayList<Long> collection_top = new ArrayList<>();
        collection.add(rule_index);
        int i = 0;
        while (i < collection.size()) {
            long current_index = collection.get(i);
            if (!NTtoNT.containsKey(current_index)) {
                for (long previous_index : NTtoNT.get(current_index)) {
                    collection.add(previous_index);
                }
            } else {
                collection_top.add(current_index);
            }
            ++i;
        }

        return collection_top;
    }

    // expanding singleton rules, e.g. S . A, A . B, B . C
    ArrayList<Long> collect_one_backwards(Long rule_index) {
        ArrayList<Long> collection = new ArrayList<>();
        collection.add(rule_index);
        int i = 0;
        while (i < collection.size()) {
            long current_index = collection.get(i);
            if (NTtoNT.containsKey(current_index)) {
                for (long previous_index : NTtoNT.get(current_index)) {
                    collection.add(previous_index);
                }
            }
            ++i;
        }

        return collection;
    }

    ArrayList< ArrayList<Long>> collect_backwards(Long child_rule_index, Long parent_rule_index) {
        HashSet<Long> visited = new HashSet<>();
        ArrayList<Long> path = new ArrayList<>();
        ArrayList< ArrayList<Long>> collection = new ArrayList<>();

        return collect_backwards(child_rule_index, parent_rule_index, visited, path, collection);
    }

    ArrayList< ArrayList<Long>> collect_backwards(long child_rule_index, long parent_rule_index, HashSet<Long> visited, ArrayList<Long> path, ArrayList< ArrayList<Long>> collection) {
        // provides all single linkage paths from a child rule to a parent rule,
        // and yes, there can be several paths

        if (!NTtoNT.containsKey(child_rule_index)) {
            return collection;
        }

        visited.add(child_rule_index);
        path.add(child_rule_index);

        for (long previous_rule : NTtoNT.get(child_rule_index)) {
            if (!visited.contains(previous_rule)) {
                if (previous_rule == parent_rule_index) {
                    ArrayList<Long> found_path = new ArrayList<>();
                    found_path.add(parent_rule_index);
                    for (int i = path.size() - 1; i >= 0; --i) {
                        found_path.add(path.get(i));
                    }
                    collection.add(found_path);
                } else {
                    collection = collect_backwards(previous_rule, parent_rule_index, visited, path, collection);
                }
            }
        }
        path.remove(path.size() - 1);
        visited.remove(child_rule_index);

        return collection;
    }

    void raise_events(TreeNode node) {
        if (node != null) {
            String node_rule_name = node.fire_event ? NTtoRule.get(node.rule_index) : "";
            if (node.fire_event) {
                parserEventHandler.handleEvent(node_rule_name + "_pre_event", node);
            }

            if (node.left != null) { // node.terminal is != None when node is leaf
                raise_events(node.left);
                if (node.right != null) {
                    raise_events(node.right);
                }
            }

            if (node.fire_event) {
                parserEventHandler.handleEvent(node_rule_name + "_post_event", node);
            }
        }
    }

    // filling the syntax tree including events
    void fill_tree(TreeNode node, DPNode dp_node) {
        // checking and extending nodes for single rule chains

        long bottom_rule = 0, top_rule = 0;
        if (dp_node.left != null) {
            bottom_rule = compute_rule_key(dp_node.rule_index_1, dp_node.rule_index_2);
            top_rule = node.rule_index;
        } else {
            top_rule = dp_node.rule_index_2;
            bottom_rule = originalTtoNT.get((char) dp_node.rule_index_1);
        }

        long subst_key = bottom_rule + (top_rule << 16);

        if ((bottom_rule != top_rule) && (substitution.containsKey(subst_key))) {
            for (long rule_index : substitution.get(subst_key)) {
                node.left = new TreeNode(rule_index, NTtoRule.containsKey(rule_index));
                node = node.left;
            }
        }

        if (dp_node.left != null) { // None => leaf
            node.left = new TreeNode(dp_node.rule_index_1, NTtoRule.containsKey(dp_node.rule_index_1));
            node.right = new TreeNode(dp_node.rule_index_2, NTtoRule.containsKey(dp_node.rule_index_2));
            fill_tree(node.left, dp_node.left);
            fill_tree(node.right, dp_node.right);
        } else {
            // I know, it is not 100% clean to store the character in an integer
            // especially when it is not the dedicated attribute for, but the heck with it!
            node.terminal = (char) dp_node.rule_index_1;
        }
    }

    public String get_error_message() {
        return errorMessage;
    }

    public T parse(String text_to_parse) {
        return parse(text_to_parse, true);
    }

    // re-implementation of Cocke-Younger-Kasami algorithm
    public T parse(String text_to_parse, boolean throw_error) {
        errorMessage = "";
        String old_text = text_to_parse;
        if (usedEof) {
            text_to_parse += EOF_SIGN;
        }
        parserEventHandler.content = null;

        parse_regular(text_to_parse);
        if (throw_error && !wordInGrammar) {
            throw new LipidParsingException("Lipid '" + old_text + "' can not be parsed by grammar '" + grammarName + "'");
        }
        T t = this.parserEventHandler.content;
        return t;
    }

    void parse_regular(String text_to_parse) {
        wordInGrammar = false;

        int n = text_to_parse.length();
        // dp stands for dynamic programming, nothing else
        ArrayList< ArrayList< HashMap<Long, DPNode>>> DP = new ArrayList<>();

        // Ks is a lookup, which fields in the DP are filled
        Bitfield[] Ks = new Bitfield[n];

        // init the tables
        for (int i = 0; i < n; ++i) {
            ArrayList< HashMap<Long, DPNode>> list = new ArrayList<>();
            for (int j = 0; j < n - i; ++j) {
                list.add(new HashMap<>());
            }
            DP.add(list);
            Ks[i] = new Bitfield(n);
        }

        boolean requirement_fulfilled = true;
        for (int i = 0; i < n; ++i) {
            char c = text_to_parse.charAt(i);
            if (!TtoNT.containsKey(c)) {
                requirement_fulfilled = false;
                break;
            }

            for (long T_rule_index : TtoNT.get(c)) {
                DPNode dp_node = new DPNode(c, T_rule_index, null, null);
                DP.get(i).get(0).put(T_rule_index, dp_node);
            }
            Ks[i].add(0);
        }

        if (requirement_fulfilled) {
            for (int i = 1; i < n; ++i) {
                int im1 = i - 1;

                for (int j = 0; j < n - i; ++j) {

                    ArrayList< HashMap<Long, DPNode>> DPj = DP.get(j);
                    HashMap<Long, DPNode> DPji = DPj.get(i);
                    int jp1 = j + 1;

                    Ks[j].resetIterator();
                    while (Ks[j].hasNext()) {
                        int k = Ks[j].next();
                        int jpok = jp1 + k;
                        int im1mk = im1 - k;
                        if (Ks[jpok].find(im1mk)) {
                            for (Entry<Long, DPNode> index_pair_1 : DP.get(j).get(k).entrySet()) {
                                Bitfield b = rightPair.get((int) (long) index_pair_1.getKey());
                                for (Entry<Long, DPNode> index_pair_2 : DP.get(jpok).get(im1mk).entrySet()) {
                                    if (b.find((int) (long) index_pair_2.getKey())) {
                                        long key = compute_rule_key(index_pair_1.getKey(), index_pair_2.getKey());

                                        DPNode content = new DPNode(index_pair_1.getKey(), index_pair_2.getKey(), index_pair_1.getValue(), index_pair_2.getValue());
                                        for (long rule_index : NTtoNT.get(key)) {
                                            if (!DPji.containsKey(rule_index)) {
                                                DPji.put(rule_index, content);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                    if (DPji.size() > 0) {
                        Ks[j].add(i);
                    }
                }
            }

            for (int i = n - 1; i > 0; --i) {
                if (DP.get(0).get(i).containsKey(START_RULE)) {
                    wordInGrammar = true;
                    TreeNode parse_tree = new TreeNode(START_RULE, NTtoRule.containsKey(START_RULE));
                    fill_tree(parse_tree, DP.get(0).get(i).get(START_RULE));
                    raise_events(parse_tree);
                    break;
                }
            }

            if (!wordInGrammar) {
                for (int i = n - 1; i > 0; --i) {
                    if (DP.get(0).get(i).size() > 0) {
                        long first_rule = DP.get(0).get(i).keySet().iterator().next();

                        TreeNode parse_tree = new TreeNode(first_rule, NTtoRule.containsKey(first_rule));
                        fill_tree(parse_tree, DP.get(0).get(i).get(first_rule));
                        errorMessage = parse_tree.getText();
                        break;
                    }
                }
            }
        }
    }

    static String readGrammarContent(String grammarResourcePath) {
        StringBuilder sb = new StringBuilder();
        // read resource from classpath and current thread's context class loader
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(grammarResourcePath)));) {
            br.lines().forEach(line -> {
                sb.append(line).append("\n");
            });
            return sb.toString();
        } catch (IOException e) {
            //always pass on the original exception
            throw new RuntimeException("Error: resource '" + grammarResourcePath + "' does not exist.", e);
        }
    }
}
