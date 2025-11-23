package com.example.smartwatchsearch.service;

import com.example.smartwatchsearch.model.Smartwatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class HTMLParser {

    // Regex for simple price validation
    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    public List<Smartwatch> parse(List<String> rawData) {
        List<Smartwatch> watches = new ArrayList<>();
        
        for (String line : rawData) {
            // Regex split to handle commas outside quotes
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // Expecting 9 columns: Brand, Model, Price, Display, Battery, Water, Conn, Sensors, Desc
            if (data.length >= 9) {
                String brand = clean(data[0]);
                String model = clean(data[1]);
                
                double price = 0.0;
                String priceStr = clean(data[2]);
                if (PRICE_PATTERN.matcher(priceStr).matches()) {
                    price = Double.parseDouble(priceStr);
                }

                String display = clean(data[3]);
                String battery = clean(data[4]);
                String water = clean(data[5]);
                String conn = clean(data[6]);
                String sensors = clean(data[7]);
                String description = clean(data[8]);

                watches.add(new Smartwatch(brand, model, price, display, battery, water, conn, sensors, description));
            }
        }
        return watches;
    }

    private String clean(String input) {
        String s = input.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
