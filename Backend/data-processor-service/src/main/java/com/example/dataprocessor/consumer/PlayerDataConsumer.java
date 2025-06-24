package com.example.dataprocessor.consumer;

import com.example.dataprocessor.model.PlayerData;
import com.example.dataprocessor.service.PlayerDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PlayerDataConsumer {

    private static final Logger log = LoggerFactory.getLogger(PlayerDataConsumer.class);

    @Autowired
    private PlayerDataService playerDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "player-data", groupId = "data-processor-group")
    public void consume(@Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            PlayerData playerData = objectMapper.readValue(message, PlayerData.class);
            log.info("Processing player data: {}", playerData);

            playerDataService.processPlayerData(playerData);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed and acknowledged message for player: {}", playerData.getName());

        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON message: {}", message, e);
            // In production, you might want to send to dead letter queue
            acknowledgment.acknowledge(); // Acknowledge to avoid reprocessing bad messages
        } catch (Exception e) {
            log.error("Error processing player data: {}", message, e);
            // Don't acknowledge - this will cause retry based on Kafka configuration
            throw new RuntimeException("Failed to process player data", e);
        }
    }

    @KafkaListener(topics = "match-data", groupId = "data-processor-group")
    public void consumeMatchData(@Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        log.info("Received match data from topic: {}", topic);

        try {
            // Process match data for ML model updates
            log.info("Processing match data: {}", message);

            // TODO: Implement match data processing logic
            // This could trigger ML model retraining

            acknowledgment.acknowledge();
            log.info("Successfully processed match data");

        } catch (Exception e) {
            log.error("Error processing match data: {}", message, e);
            throw new RuntimeException("Failed to process match data", e);
        }
    }
}