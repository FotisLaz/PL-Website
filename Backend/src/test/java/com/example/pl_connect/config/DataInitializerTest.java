package com.example.pl_connect.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DataInitializerTest {

    @Test
    void keepsQuotedPositionAsOneField() {
        String[] fields = DataInitializer.splitCsvLine(
                "Eddie Nketiah,eng ENG,\"FW,MF\",23,12,0,152.0,0.0,0.0,0.0,0.0,0.0,0.7,0.2,Arsenal");

        assertThat(fields).hasSize(15);
        assertThat(fields[0]).isEqualTo("Eddie Nketiah");
        assertThat(fields[2]).isEqualTo("FW,MF");
        assertThat(fields[3]).isEqualTo("23");
        assertThat(fields[14]).isEqualTo("Arsenal");
    }

    @Test
    void keepsTrailingEmptyFields() {
        String[] fields = DataInitializer.splitCsvLine(
                "Nicolas Pépé,ci CIV,\"FW,MF\",27,0,0,,,,,,,,,Arsenal");

        assertThat(fields).hasSize(15);
        assertThat(fields[6]).isEmpty();
        assertThat(fields[14]).isEqualTo("Arsenal");

        assertThat(DataInitializer.splitCsvLine("a,b,")).containsExactly("a", "b", "");
    }

    @Test
    void treatsDoubledQuoteInsideQuotesAsLiteralQuote() {
        assertThat(DataInitializer.splitCsvLine("\"say \"\"hi\"\"\",x"))
                .containsExactly("say \"hi\"", "x");
    }

    @Test
    void parsesEveryPlayerRowOfTheBundledCsv() throws Exception {
        int rows = 0;
        Set<String> names = new HashSet<>();
        ClassPathResource resource = new ClassPathResource("data/prem_stats.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = DataInitializer.splitCsvLine(line);
                if (fields.length == 15 && !"Squad Total".equals(fields[0])) {
                    rows++;
                    names.add(fields[0]);
                }
            }
        }

        // 587 player rows; 12 players appear twice (mid-season transfers), and the name is the primary key
        assertThat(rows).isEqualTo(587);
        assertThat(names).hasSize(575);
    }
}
