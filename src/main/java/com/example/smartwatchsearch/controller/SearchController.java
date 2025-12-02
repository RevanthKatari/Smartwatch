package com.example.smartwatchsearch.controller;

import com.example.smartwatchsearch.model.Smartwatch;
import com.example.smartwatchsearch.service.SearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchEngine searchEngine;

    @GetMapping("/search")
    public SearchEngine.SearchResult search(@RequestParam String query) {
        return searchEngine.performSearch(query);
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String prefix) {
        return searchEngine.getSuggestions(prefix);
    }
    
    @GetMapping("/history")
    public Map<String, Integer> getHistory() {
        return searchEngine.getSearchHistory();
    }

    @PostMapping("/recommend")
    public List<Smartwatch> recommend(@RequestBody com.example.smartwatchsearch.model.RecommendationRequest request) {
        return searchEngine.getRecommendations(request);
    }
    
    @GetMapping("/watches")
    public List<Smartwatch> getAllWatches() {
        return searchEngine.getAllWatches();
    }
    
    @GetMapping("/brands")
    public List<String> getAllBrands() {
        return searchEngine.getAllBrands();
    }
}

