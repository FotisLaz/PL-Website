package com.example.dataprocessor.consumer;

import com.example.dataprocessor.model.PlayerData;
import com.example.dataprocessor.service.PlayerDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PlayerDataConsumerTest {

    @Mock
    private PlayerDataService playerDataService;

    // Real JSON parsing, so the tests also check the PlayerData mapping
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private PlayerDataConsumer consumer;

    private static final String VALID_JSON =
            "{\"name\":\"Bukayo Saka\",\"team\":\"Arsenal\",\"pos\":\"FW\",\"age\":22,"
                    + "\"gls\":16.0,\"xg\":14.5,\"unknownField\":\"ignored\"}";

    @Test
    void validMessageIsProcessedAndAcknowledged() {
        consumer.consume(VALID_JSON, "player-data", 0, 42L, acknowledgment);

        ArgumentCaptor<PlayerData> data = ArgumentCaptor.forClass(PlayerData.class);
        verify(playerDataService).processPlayerData(data.capture());
        assertThat(data.getValue().getName()).isEqualTo("Bukayo Saka");
        assertThat(data.getValue().getTeam()).isEqualTo("Arsenal");
        assertThat(data.getValue().getAge()).isEqualTo(22);
        assertThat(data.getValue().getGls()).isEqualTo(16.0);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void invalidJsonIsAcknowledgedAndSkipped() {
        consumer.consume("not json", "player-data", 0, 43L, acknowledgment);

        verifyNoInteractions(playerDataService);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void processingErrorIsRethrownAndNotAcknowledged() {
        doThrow(new RuntimeException("db down")).when(playerDataService).processPlayerData(any());

        assertThatThrownBy(() -> consumer.consume(VALID_JSON, "player-data", 0, 44L, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to process player data");
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void matchDataIsAcknowledged() {
        consumer.consumeMatchData("{\"home\":\"Arsenal\"}", "match-data", acknowledgment);

        verify(acknowledgment).acknowledge();
        verifyNoInteractions(playerDataService);
    }
}
