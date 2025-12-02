# Smartwatch Recommendation System

## Features
- **Web Crawler**: Simulates crawling data from CSV source.
- **HTML Parser**: Parses structured data using Regular Expressions.
- **Search Engine**: 
  - Inverted Indexing for O(1) lookup.
  - Page Ranking based on keyword frequency.
  - Spell Checking using Levenshtein Edit Distance.
  - Word Completion using Trie data structure.
- **Web UI**: Modern, responsive interface.

## How to Run
1. Open Terminal.
2. Run the following command:
   ```bash
   mvn spring-boot:run
   ```
3. Open your browser and go to: [http://localhost:8080](http://localhost:8080)

## Project Structure
- `src/main/java`: Backend Java code (Spring Boot).
- `src/main/resources/static`: Frontend HTML/CSS/JS.
- `smartwatches.csv`: The dataset.

