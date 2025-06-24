package com.example.pl_connect.integration;

import com.example.pl_connect.player.Player;
import com.example.pl_connect.player.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PlayerIntegrationTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private PlayerRepository playerRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private String baseUrl;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/v1/player";

                // Clear all existing data
                playerRepository.deleteAll();

                // Create test data
                Player player1 = new Player();
                player1.setName("Bukayo Saka");
                player1.setNation("England");
                player1.setPos("RW");
                player1.setAge(22);
                player1.setMp(15);
                player1.setStarts(15);
                player1.setMin(1350.0);
                player1.setGls(5.0);
                player1.setAst(8.0);
                player1.setPk(1.0);
                player1.setCrdy(2.0);
                player1.setCrdr(0.0);
                player1.setXg(4.5);
                player1.setXag(6.2);
                player1.setTeam("Arsenal");

                Player player2 = new Player();
                player2.setName("Martin Ødegaard");
                player2.setNation("Norway");
                player2.setPos("CAM");
                player2.setAge(25);
                player2.setMp(14);
                player2.setStarts(14);
                player2.setMin(1260.0);
                player2.setGls(4.0);
                player2.setAst(6.0);
                player2.setPk(0.0);
                player2.setCrdy(1.0);
                player2.setCrdr(0.0);
                player2.setXg(3.8);
                player2.setXag(5.9);
                player2.setTeam("Arsenal");

                playerRepository.save(player1);
                playerRepository.save(player2);
        }

        @Test
        void shouldGetAllPlayers() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl,
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(2);
                assertThat(response.getBody())
                                .extracting(Player::getName)
                                .containsExactlyInAnyOrder("Bukayo Saka", "Martin Ødegaard");
        }

        @Test
        void shouldGetPlayersByTeam() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl + "?team=Arsenal",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(2);
                assertThat(response.getBody())
                                .extracting(Player::getTeam)
                                .containsOnly("Arsenal");
        }

        @Test
        void shouldGetPlayersByPosition() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl + "?position=RW",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).getName()).isEqualTo("Bukayo Saka");
        }

        @Test
        void shouldGetPlayersByNation() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl + "?nation=England",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).getName()).isEqualTo("Bukayo Saka");
        }

        @Test
        void shouldSearchPlayersByName() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl + "?name=saka",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).getName()).isEqualTo("Bukayo Saka");
        }

        @Test
        void shouldAddNewPlayer() {
                // Given
                Player newPlayer = new Player();
                newPlayer.setName("Declan Rice");
                newPlayer.setNation("England");
                newPlayer.setPos("DM");
                newPlayer.setAge(24);
                newPlayer.setMp(15);
                newPlayer.setStarts(15);
                newPlayer.setMin(1350.0);
                newPlayer.setGls(2.0);
                newPlayer.setAst(3.0);
                newPlayer.setPk(0.0);
                newPlayer.setCrdy(4.0);
                newPlayer.setCrdr(0.0);
                newPlayer.setXg(1.8);
                newPlayer.setXag(2.2);
                newPlayer.setTeam("Arsenal");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Player> request = new HttpEntity<>(newPlayer, headers);

                // When
                ResponseEntity<Player> response = restTemplate.postForEntity(baseUrl, request, Player.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody().getName()).isEqualTo("Declan Rice");
                assertThat(response.getBody().getTeam()).isEqualTo("Arsenal");

                // Verify it was actually saved
                ResponseEntity<List<Player>> getResponse = restTemplate.exchange(
                                baseUrl + "?name=Declan Rice",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });
                assertThat(getResponse.getBody()).hasSize(1);
                assertThat(getResponse.getBody().get(0).getName()).isEqualTo("Declan Rice");
        }

        @Test
        void shouldUpdateExistingPlayer() {
                // Given
                Player updatedPlayer = new Player();
                updatedPlayer.setName("Bukayo Saka");
                updatedPlayer.setNation("England");
                updatedPlayer.setPos("LW"); // Position changed
                updatedPlayer.setAge(22);
                updatedPlayer.setMp(15);
                updatedPlayer.setStarts(15);
                updatedPlayer.setMin(1350.0);
                updatedPlayer.setGls(5.0);
                updatedPlayer.setAst(8.0);
                updatedPlayer.setPk(1.0);
                updatedPlayer.setCrdy(2.0);
                updatedPlayer.setCrdr(0.0);
                updatedPlayer.setXg(4.5);
                updatedPlayer.setXag(6.2);
                updatedPlayer.setTeam("Arsenal");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Player> request = new HttpEntity<>(updatedPlayer, headers);

                // When
                ResponseEntity<Player> response = restTemplate.exchange(
                                baseUrl,
                                HttpMethod.PUT,
                                request,
                                Player.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getName()).isEqualTo("Bukayo Saka");
                assertThat(response.getBody().getPos()).isEqualTo("LW");

                // Verify the update persisted
                ResponseEntity<List<Player>> getResponse = restTemplate.exchange(
                                baseUrl + "?position=LW",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });
                assertThat(getResponse.getBody()).hasSize(1);
                assertThat(getResponse.getBody().get(0).getName()).isEqualTo("Bukayo Saka");
        }

        @Test
        void shouldDeletePlayer() {
                // When
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/Bukayo Saka",
                                HttpMethod.DELETE,
                                null,
                                String.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo("Player deleted successfully");

                // Verify player was deleted
                ResponseEntity<List<Player>> getResponse = restTemplate.exchange(
                                baseUrl + "?name=Bukayo Saka",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });
                assertThat(getResponse.getBody()).hasSize(0);
        }

        @Test
        void shouldFilterPlayersByTeamAndPosition() {
                // When
                ResponseEntity<List<Player>> response = restTemplate.exchange(
                                baseUrl + "?team=Arsenal&position=RW",
                                HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<List<Player>>() {
                                });

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).getName()).isEqualTo("Bukayo Saka");
                assertThat(response.getBody().get(0).getTeam()).isEqualTo("Arsenal");
                assertThat(response.getBody().get(0).getPos()).isEqualTo("RW");
        }
}