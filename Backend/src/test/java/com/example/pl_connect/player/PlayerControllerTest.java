package com.example.pl_connect.player;

import com.example.pl_connect.controller.PlayerController;
import com.example.pl_connect.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = new Player();
        testPlayer1.setName("Bukayo Saka");
        testPlayer1.setNation("England");
        testPlayer1.setPos("RW");
        testPlayer1.setAge(22);
        testPlayer1.setMp(15);
        testPlayer1.setStarts(15);
        testPlayer1.setMin(1350.0);
        testPlayer1.setGls(5.0);
        testPlayer1.setAst(8.0);
        testPlayer1.setPk(1.0);
        testPlayer1.setCrdy(2.0);
        testPlayer1.setCrdr(0.0);
        testPlayer1.setXg(4.5);
        testPlayer1.setXag(6.2);
        testPlayer1.setTeam("Arsenal");

        testPlayer2 = new Player();
        testPlayer2.setName("Cole Palmer");
        testPlayer2.setNation("England");
        testPlayer2.setPos("RW");
        testPlayer2.setAge(21);
        testPlayer2.setMp(16);
        testPlayer2.setStarts(16);
        testPlayer2.setMin(1440.0);
        testPlayer2.setGls(7.0);
        testPlayer2.setAst(5.0);
        testPlayer2.setPk(2.0);
        testPlayer2.setCrdy(3.0);
        testPlayer2.setCrdr(0.0);
        testPlayer2.setXg(6.1);
        testPlayer2.setXag(4.8);
        testPlayer2.setTeam("Chelsea");
    }

    @Test
    void shouldGetAllPlayersWhenNoParametersProvided() throws Exception {
        // Given
        List<Player> players = Arrays.asList(testPlayer1, testPlayer2);
        when(playerService.getPlayers()).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/v1/player"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Bukayo Saka")))
                .andExpect(jsonPath("$[0].team", is("Arsenal")))
                .andExpect(jsonPath("$[1].name", is("Cole Palmer")))
                .andExpect(jsonPath("$[1].team", is("Chelsea")));

        verify(playerService).getPlayers();
    }

    @Test
    void shouldGetPlayersByTeam() throws Exception {
        // Given
        List<Player> arsenalPlayers = Collections.singletonList(testPlayer1);
        when(playerService.getPlayersFromTeam("Arsenal")).thenReturn(arsenalPlayers);

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("team", "Arsenal"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bukayo Saka")))
                .andExpect(jsonPath("$[0].team", is("Arsenal")));

        verify(playerService).getPlayersFromTeam("Arsenal");
    }

    @Test
    void shouldGetPlayersByName() throws Exception {
        // Given
        List<Player> players = Collections.singletonList(testPlayer1);
        when(playerService.getPlayersByName("Saka")).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("name", "Saka"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bukayo Saka")));

        verify(playerService).getPlayersByName("Saka");
    }

    @Test
    void shouldGetPlayersByPosition() throws Exception {
        // Given
        List<Player> players = Arrays.asList(testPlayer1, testPlayer2);
        when(playerService.getPlayersByPos("RW")).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("position", "RW"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].pos", everyItem(is("RW"))));

        verify(playerService).getPlayersByPos("RW");
    }

    @Test
    void shouldGetPlayersByNation() throws Exception {
        // Given
        List<Player> players = Arrays.asList(testPlayer1, testPlayer2);
        when(playerService.getPlayersByNation("England")).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("nation", "England"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nation", everyItem(is("England"))));

        verify(playerService).getPlayersByNation("England");
    }

    @Test
    void shouldGetPlayersByTeamAndPosition() throws Exception {
        // Given
        List<Player> players = Collections.singletonList(testPlayer1);
        when(playerService.getPlayersByTeamAndPosition("Arsenal", "RW")).thenReturn(players);

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("team", "Arsenal")
                .param("position", "RW"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Bukayo Saka")))
                .andExpect(jsonPath("$[0].team", is("Arsenal")))
                .andExpect(jsonPath("$[0].pos", is("RW")));

        verify(playerService).getPlayersByTeamAndPosition("Arsenal", "RW");
    }

    @Test
    void shouldAddNewPlayer() throws Exception {
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

        when(playerService.addPlayer(any(Player.class))).thenReturn(newPlayer);

        // When & Then
        mockMvc.perform(post("/api/v1/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPlayer)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Declan Rice")))
                .andExpect(jsonPath("$.team", is("Arsenal")))
                .andExpect(jsonPath("$.pos", is("DM")));

        verify(playerService).addPlayer(any(Player.class));
    }

    @Test
    void shouldUpdateExistingPlayer() throws Exception {
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

        when(playerService.updatePlayer(any(Player.class))).thenReturn(updatedPlayer);

        // When & Then
        mockMvc.perform(put("/api/v1/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPlayer)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Bukayo Saka")))
                .andExpect(jsonPath("$.pos", is("LW")));

        verify(playerService).updatePlayer(any(Player.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPlayer() throws Exception {
        // Given
        Player nonExistentPlayer = new Player();
        nonExistentPlayer.setName("Non Existent Player");
        nonExistentPlayer.setNation("Unknown");
        nonExistentPlayer.setPos("Unknown");
        nonExistentPlayer.setAge(25);
        nonExistentPlayer.setMp(0);
        nonExistentPlayer.setStarts(0);
        nonExistentPlayer.setMin(0.0);
        nonExistentPlayer.setGls(0.0);
        nonExistentPlayer.setAst(0.0);
        nonExistentPlayer.setPk(0.0);
        nonExistentPlayer.setCrdy(0.0);
        nonExistentPlayer.setCrdr(0.0);
        nonExistentPlayer.setXg(0.0);
        nonExistentPlayer.setXag(0.0);
        nonExistentPlayer.setTeam("Unknown");

        when(playerService.updatePlayer(any(Player.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/v1/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentPlayer)))
                .andExpect(status().isNotFound());

        verify(playerService).updatePlayer(any(Player.class));
    }

    @Test
    void shouldDeletePlayer() throws Exception {
        // Given
        doNothing().when(playerService).deletePlayer("Bukayo Saka");

        // When & Then
        mockMvc.perform(delete("/api/v1/player/Bukayo Saka"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(playerService).deletePlayer("Bukayo Saka");
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersFound() throws Exception {
        // Given
        when(playerService.getPlayersFromTeam("NonExistentTeam")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/player")
                .param("team", "NonExistentTeam"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(playerService).getPlayersFromTeam("NonExistentTeam");
    }
}