package com.example.smartwatchsearch.model;

public class Smartwatch {
    private String brand;
    private String model;
    private double price;
    private String display;
    private String battery;
    private String waterResistance;
    private String connectivity;
    private String sensors;
    private String description;
    private int searchMatchCount; // For page ranking

    public Smartwatch(String brand, String model, double price, String display, String battery, 
                      String waterResistance, String connectivity, String sensors, String description) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.display = display;
        this.battery = battery;
        this.waterResistance = waterResistance;
        this.connectivity = connectivity;
        this.sensors = sensors;
        this.description = description;
        this.searchMatchCount = 0;
    }

    // Getters and Setters
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public String getDisplay() { return display; }
    public String getBattery() { return battery; }
    public String getWaterResistance() { return waterResistance; }
    public String getConnectivity() { return connectivity; }
    public String getSensors() { return sensors; }
    public String getDescription() { return description; }
    
    public int getSearchMatchCount() { return searchMatchCount; }
    public void setSearchMatchCount(int searchMatchCount) { this.searchMatchCount = searchMatchCount; }
}
