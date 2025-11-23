package com.example.smartwatchsearch.util;

import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
}

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode current = root;
        for (char l : word.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(l, c -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    public List<String> autocomplete(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode current = root;
        for (char l : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(l);
            if (current == null) return results;
        }
        findWords(current, prefix.toLowerCase(), results);
        return results;
    }

    private void findWords(TrieNode node, String currentWord, List<String> results) {
        if (node.isEndOfWord) {
            results.add(currentWord);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            findWords(entry.getValue(), currentWord + entry.getKey(), results);
        }
    }
}

