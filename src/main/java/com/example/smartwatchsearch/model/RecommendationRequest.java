package com.example.smartwatchsearch.model;

import java.util.List;

public class RecommendationRequest {
    private Double minPrice;
    private Double maxPrice;
    private List<String> brands;
    private List<String> features; // e.g., "GPS", "Waterproof", "ECG"

    // Getters and Setters
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public List<String> getBrands() { return brands; }
    public void setBrands(List<String> brands) { this.brands = brands; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}

