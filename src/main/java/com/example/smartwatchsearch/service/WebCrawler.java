package com.example.smartwatchsearch.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebCrawler {

    public List<String> crawl(String filePath) {
        List<String> rawLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header
            br.readLine();
            while ((line = br.readLine()) != null) {
                rawLines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error crawling file: " + e.getMessage());
        }
        return rawLines;
    }
}

