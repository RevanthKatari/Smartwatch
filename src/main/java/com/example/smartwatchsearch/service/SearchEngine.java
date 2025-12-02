package com.example.smartwatchsearch.service;

import com.example.smartwatchsearch.model.RecommendationRequest;
import com.example.smartwatchsearch.model.Smartwatch;
import com.example.smartwatchsearch.util.Trie;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchEngine {

    @Autowired
    private WebCrawler crawler;

    @Autowired
    private HTMLParser parser;

    private List<Smartwatch> allWatches = new ArrayList<>();
    private Map<String, List<Integer>> invertedIndex = new HashMap<>();
    private Trie wordCompletionTrie = new Trie();
    private Map<String, Integer> searchHistory = new HashMap<>();
    private Set<String> vocabulary = new HashSet<>();

    // Regex patterns for features
    private static final Map<String, Pattern> FEATURE_PATTERNS = new HashMap<>();
    static {
        FEATURE_PATTERNS.put("Heart Rate", Pattern.compile("heart rate|hr|pulse|ecg|cardio", Pattern.CASE_INSENSITIVE));
        FEATURE_PATTERNS.put("GPS", Pattern.compile("gps|location|navigation|map|route|gnss", Pattern.CASE_INSENSITIVE));
        FEATURE_PATTERNS.put("Waterproof", Pattern.compile("water|swim|dive|atm|resistant|ip68|scuba|marine", Pattern.CASE_INSENSITIVE));
        FEATURE_PATTERNS.put("Battery", Pattern.compile("battery|day|hour|life|solar|unlimited", Pattern.CASE_INSENSITIVE));
        FEATURE_PATTERNS.put("Sleep", Pattern.compile("sleep|nap|rest|recovery|night", Pattern.CASE_INSENSITIVE));
        FEATURE_PATTERNS.put("LTE", Pattern.compile("lte|cellular|call|esim|4g", Pattern.CASE_INSENSITIVE));
    }

    @PostConstruct
    public void init() {
        // Initialize system on startup
        List<String> rawData = crawler.crawl("smartwatches.csv");
        allWatches = parser.parse(rawData);
        
        for (int i = 0; i < allWatches.size(); i++) {
            indexWatch(allWatches.get(i), i);
        }
        System.out.println("Search Engine Initialized with " + allWatches.size() + " items.");
    }

    private void indexWatch(Smartwatch sw, int index) {
        String text = sw.getBrand() + " " + sw.getModel() + " " + sw.getDescription() + " " + sw.getSensors() + " " + sw.getDisplay();
        Pattern p = Pattern.compile("[a-zA-Z0-9]+");
        Matcher m = p.matcher(text);

        while (m.find()) {
            String word = m.group().toLowerCase();
            vocabulary.add(word);
            wordCompletionTrie.insert(word);

            invertedIndex.putIfAbsent(word, new ArrayList<>());
            if (!invertedIndex.get(word).contains(index)) {
                invertedIndex.get(word).add(index);
            }
        }
    }

    public List<String> getSuggestions(String prefix) {
        return wordCompletionTrie.autocomplete(prefix);
    }

    public SearchResult performSearch(String query) {
        searchHistory.put(query, searchHistory.getOrDefault(query, 0) + 1);
        String cleanQuery = query.toLowerCase().trim();
        
        // Support multi-word queries
        String[] queryWords = cleanQuery.split("\\s+");
        List<Smartwatch> results = new ArrayList<>();
        Map<Integer, Integer> watchScores = new HashMap<>();
        String suggestion = null;

        // Handle multi-word search
        if (queryWords.length > 1) {
            // Search for all words and combine results
            Set<Integer> candidateIndices = new HashSet<>();
            
            for (String word : queryWords) {
                String correctedWord = word;
                // Spell check each word
                if (!invertedIndex.containsKey(word)) {
                    String corrected = suggestCorrection(word);
                    if (corrected != null && !corrected.equals(word)) {
                        correctedWord = corrected;
                        if (suggestion == null) {
                            suggestion = query.replace(word, corrected);
                        }
                    }
                }
                
                List<Integer> indices = invertedIndex.get(correctedWord);
                if (indices != null) {
                    candidateIndices.addAll(indices);
                }
            }
            
            // Score watches based on how many query words they match
            for (int idx : candidateIndices) {
                Smartwatch sw = allWatches.get(idx);
                int score = 0;
                String searchText = (sw.getBrand() + " " + sw.getModel() + " " + sw.getDescription() + " " + sw.getSensors() + " " + sw.getDisplay()).toLowerCase();
                
                for (String word : queryWords) {
                    if (searchText.contains(word)) {
                        score += countOccurrences(sw, word);
                    }
                }
                
                // Bonus for phrase matching (consecutive words)
                if (searchText.contains(cleanQuery)) {
                    score += 5;
                }
                
                watchScores.put(idx, score);
            }
            
            // Add watches with scores > 0
            for (Map.Entry<Integer, Integer> entry : watchScores.entrySet()) {
                if (entry.getValue() > 0) {
                    Smartwatch sw = allWatches.get(entry.getKey());
                    sw.setSearchMatchCount(entry.getValue());
                    results.add(sw);
                }
            }
        } else {
            // Single word search (original logic)
            String usedQuery = cleanQuery;
            
            // Spell Checking / Correction
            if (!invertedIndex.containsKey(cleanQuery)) {
                String corrected = suggestCorrection(cleanQuery);
                if (corrected != null && !corrected.equals(cleanQuery)) {
                    suggestion = corrected;
                    usedQuery = corrected;
                }
            }

            List<Integer> indices = invertedIndex.get(usedQuery);
            
            if (indices != null) {
                for (int idx : indices) {
                    Smartwatch sw = allWatches.get(idx);
                    // Frequency Count (Page Ranking)
                    sw.setSearchMatchCount(countOccurrences(sw, usedQuery));
                    results.add(sw);
                }
            }
        }
        
        // Sort by rank
        results.sort((s1, s2) -> Integer.compare(s2.getSearchMatchCount(), s1.getSearchMatchCount()));

        return new SearchResult(results, suggestion);
    }

    private int countOccurrences(Smartwatch sw, String word) {
        String text = (sw.getBrand() + " " + sw.getModel() + " " + sw.getDescription() + " " + sw.getSensors()).toLowerCase();
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            count++;
            idx += word.length();
        }
        return count;
    }

    private String suggestCorrection(String input) {
        if (vocabulary.contains(input)) return input;
        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (String word : vocabulary) {
            int distance = calculateEditDistance(input, word);
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = word;
            }
        }
        return (minDistance <= 2) ? bestMatch : null;
    }

    private int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[s1.length()][s2.length()];
    }
    
    public List<Smartwatch> getRecommendations(RecommendationRequest request) {
        // Reset scores
        for (Smartwatch sw : allWatches) sw.setSearchMatchCount(0);

        return allWatches.stream()
            .filter(sw -> {
                // Filter by Price
                if (request.getMinPrice() != null && sw.getPrice() < request.getMinPrice()) return false;
                if (request.getMaxPrice() != null && sw.getPrice() > request.getMaxPrice()) return false;
                return true;
            })
            .peek(sw -> {
                int score = 0;
                
                // Score by Brand
                if (request.getBrands() != null && !request.getBrands().isEmpty()) {
                    for (String brand : request.getBrands()) {
                        if (sw.getBrand().equalsIgnoreCase(brand)) {
                            score += 10;
                            break;
                        }
                    }
                }

                // Score by Features (Regex Match in Description)
                if (request.getFeatures() != null) {
                    String desc = (sw.getDescription() + " " + sw.getModel()).toLowerCase();
                    for (String feature : request.getFeatures()) {
                        Pattern p = FEATURE_PATTERNS.get(feature);
                        if (p == null) p = Pattern.compile(Pattern.quote(feature), Pattern.CASE_INSENSITIVE);
                        
                        if (p.matcher(desc).find()) {
                            score += 5;
                        }
                    }
                }
                sw.setSearchMatchCount(score);
            })
            .sorted((s1, s2) -> Integer.compare(s2.getSearchMatchCount(), s1.getSearchMatchCount()))
            .limit(10)
            .collect(Collectors.toList());
    }

    public Map<String, Integer> getSearchHistory() {
        return searchHistory;
    }
    
    public List<Smartwatch> getAllWatches() {
        return new ArrayList<>(allWatches);
    }
    
    public List<String> getAllBrands() {
        return allWatches.stream()
            .map(Smartwatch::getBrand)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Extract numeric battery life in days (approximate)
     */
    public static double extractBatteryDays(String batteryStr) {
        if (batteryStr == null || batteryStr.isEmpty()) return 0;
        
        Pattern dayPattern = Pattern.compile("(\\d+)\\s*days?", Pattern.CASE_INSENSITIVE);
        Pattern hourPattern = Pattern.compile("(\\d+)\\s*hours?", Pattern.CASE_INSENSITIVE);
        
        Matcher dayMatcher = dayPattern.matcher(batteryStr);
        if (dayMatcher.find()) {
            return Double.parseDouble(dayMatcher.group(1));
        }
        
        Matcher hourMatcher = hourPattern.matcher(batteryStr);
        if (hourMatcher.find()) {
            return Double.parseDouble(hourMatcher.group(1)) / 24.0;
        }
        
        // Check for "unlimited" or "solar" which might indicate very long battery
        if (batteryStr.toLowerCase().contains("unlimited") || batteryStr.toLowerCase().contains("solar")) {
            return 999; // High value for sorting
        }
        
        return 0;
    }

    // Helper class for return values
    public static class SearchResult {
        public List<Smartwatch> watches;
        public String suggestion;

        public SearchResult(List<Smartwatch> watches, String suggestion) {
            this.watches = watches;
            this.suggestion = suggestion;
        }
    }
}

