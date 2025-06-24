package com.example.pl_connect.config;

import com.example.pl_connect.player.Player;
import com.example.pl_connect.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String CSV_FILE_PATH = "data/prem_stats.csv";

    @Bean
    @Transactional // Ensure the operation runs within a transaction
    CommandLineRunner initDatabase(PlayerRepository repository) {
        return args -> {
            if (repository.count() == 0) { // Load data only if table is empty
                log.info("Database is empty. Preloading data from {}", CSV_FILE_PATH);
                try {
                    ClassPathResource resource = new ClassPathResource(CSV_FILE_PATH);
                    if (!resource.exists()) {
                        log.error("CSV file not found at path: {}", CSV_FILE_PATH);
                        return;
                    }

                    List<Player> players = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        String line = reader.readLine(); // Skip header line
                        if (line == null) {
                            log.warn("CSV file is empty or header is missing.");
                            return;
                        }

                        while ((line = reader.readLine()) != null) {
                            String[] data = line.split(",", -1); // -1 to keep trailing empty strings

                            if (data.length != 15 || "Squad Total".equals(data[0])) {
                                log.warn("Skipping malformed or summary line: {}", line);
                                continue;
                            }

                            try {
                                Player player = new Player();
                                player.setName(data[0]); // Corresponds to 'name' field
                                player.setNation(data[1]);
                                player.setPos(data[2]); // Corresponds to 'pos' field
                                player.setAge(parseInteger(data[3]));
                                player.setMp(parseInteger(data[4])); // Corresponds to 'mp' field
                                player.setStarts(parseInteger(data[5]));
                                player.setMin(parseDouble(data[6])); // Corresponds to 'min' field
                                player.setGls(parseDouble(data[7])); // Corresponds to 'gls' field
                                player.setAst(parseDouble(data[8])); // Corresponds to 'ast' field
                                player.setPk(parseDouble(data[9])); // Corresponds to 'pk' field
                                player.setCrdy(parseDouble(data[10])); // Corresponds to 'crdy' field
                                player.setCrdr(parseDouble(data[11])); // Corresponds to 'crdr' field
                                player.setXg(parseDouble(data[12])); // Corresponds to 'xg' field
                                player.setXag(parseDouble(data[13])); // Corresponds to 'xag' field
                                player.setTeam(data[14]); // Corresponds to 'team' field

                                players.add(player);
                            } catch (NumberFormatException e) {
                                log.error("Error parsing number in line: {}. Error: {}", line, e.getMessage());
                            } catch (ArrayIndexOutOfBoundsException e) {
                                log.error("Incorrect number of columns in line: {}", line);
                            }
                        }
                    }

                    if (!players.isEmpty()) {
                        repository.saveAll(players);
                        log.info("Preloaded {} players into the database.", players.size());
                    } else {
                        log.warn("No valid player data found in the CSV file.");
                    }

                } catch (Exception e) {
                    log.error("Error during database preloading from CSV: {}", e.getMessage(), e);
                }
            } else {
                log.info("Database already contains data. Skipping preloading.");
            }
        };
    }

    // Helper methods for safe number parsing
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0; // Return 0 instead of null for empty/invalid integers
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Could not parse integer value: '{}'. Defaulting to 0.", value);
            return 0; // Return 0 instead of null
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0; // Return 0.0 instead of null for empty/invalid doubles
        }
        try {
            // Remove potential non-breaking spaces and trim
            String cleanedValue = value.trim().replace("\u00A0", "");
            if (cleanedValue.isEmpty()) return 0.0; // Also return 0.0 if empty after cleaning
            return Double.parseDouble(cleanedValue);
        } catch (NumberFormatException e) {
            log.warn("Could not parse double value: '{}'. Defaulting to 0.0.", value);
            return 0.0; // Return 0.0 instead of null
        }
    }
} 