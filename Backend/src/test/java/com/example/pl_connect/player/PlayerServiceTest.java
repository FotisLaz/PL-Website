package com.example.pl_connect.player;

import com.example.pl_connect.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player testPlayer1;
    private Player testPlayer2;
    private Player testPlayer3;

    @BeforeEach
    void setUp() {
        testPlayer1 = new Player();
        testPlayer1.setName("Bukayo Saka");
        testPlayer1.setNation("England");
        testPlayer1.setPos("RW");
        testPlayer1.setAge(22);
        testPlayer1.setTeam("Arsenal");

        testPlayer2 = new Player();
        testPlayer2.setName("Martin Ødegaard");
        testPlayer2.setNation("Norway");
        testPlayer2.setPos("CAM");
        testPlayer2.setAge(25);
        testPlayer2.setTeam("Arsenal");

        testPlayer3 = new Player();
        testPlayer3.setName("Cole Palmer");
        testPlayer3.setNation("England");
        testPlayer3.setPos("RW");
        testPlayer3.setAge(21);
        testPlayer3.setTeam("Chelsea");
    }

    @Test
    void shouldGetAllPlayers() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayers();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(testPlayer1, testPlayer2, testPlayer3);
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersFromTeam() {
        // Given
        List<Player> arsenalPlayers = Arrays.asList(testPlayer1, testPlayer2);
        when(playerRepository.findByTeam("Arsenal")).thenReturn(arsenalPlayers);

        // When
        List<Player> result = playerService.getPlayersFromTeam("Arsenal");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testPlayer1, testPlayer2);
        verify(playerRepository).findByTeam("Arsenal");
    }

    @Test
    void shouldGetPlayersByName() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByName("saka");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bukayo Saka");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByNameCaseInsensitive() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByName("BUKAYO");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bukayo Saka");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByPosition() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByPos("RW");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Bukayo Saka", "Cole Palmer");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByPositionCaseInsensitive() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByPos("cam");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Martin Ødegaard");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByNation() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByNation("England");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Bukayo Saka", "Cole Palmer");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByNationCaseInsensitive() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByNation("norway");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Martin Ødegaard");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldGetPlayersByTeamAndPosition() {
        // Given
        List<Player> allPlayers = Arrays.asList(testPlayer1, testPlayer2, testPlayer3);
        when(playerRepository.findAll()).thenReturn(allPlayers);

        // When
        List<Player> result = playerService.getPlayersByTeamAndPosition("Arsenal", "RW");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bukayo Saka");
        verify(playerRepository).findAll();
    }

    @Test
    void shouldAddPlayer() {
        // Given
        Player newPlayer = new Player();
        newPlayer.setName("Declan Rice");
        newPlayer.setTeam("Arsenal");
        when(playerRepository.save(any(Player.class))).thenReturn(newPlayer);

        // When
        Player result = playerService.addPlayer(newPlayer);

        // Then
        assertThat(result.getName()).isEqualTo("Declan Rice");
        assertThat(result.getTeam()).isEqualTo("Arsenal");
        verify(playerRepository).save(newPlayer);
    }

    @Test
    void shouldUpdateExistingPlayer() {
        // Given
        Player existingPlayer = new Player();
        existingPlayer.setName("Bukayo Saka");
        existingPlayer.setTeam("Arsenal");
        existingPlayer.setPos("RW");
        existingPlayer.setNation("England");

        Player updatedPlayer = new Player();
        updatedPlayer.setName("Bukayo Saka");
        updatedPlayer.setTeam("Arsenal");
        updatedPlayer.setPos("LW"); // Position changed
        updatedPlayer.setNation("England");

        when(playerRepository.findByName("Bukayo Saka")).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        // When
        Player result = playerService.updatePlayer(updatedPlayer);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPos()).isEqualTo("LW");
        verify(playerRepository).findByName("Bukayo Saka");
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void shouldReturnNullWhenUpdatingNonExistentPlayer() {
        // Given
        Player nonExistentPlayer = new Player();
        nonExistentPlayer.setName("Non Existent Player");
        when(playerRepository.findByName("Non Existent Player")).thenReturn(Optional.empty());

        // When
        Player result = playerService.updatePlayer(nonExistentPlayer);

        // Then
        assertThat(result).isNull();
        verify(playerRepository).findByName("Non Existent Player");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void shouldDeletePlayer() {
        // When
        playerService.deletePlayer("Bukayo Saka");

        // Then
        verify(playerRepository).deleteByName("Bukayo Saka");
    }

    @Test
    void shouldReturnEmptyListWhenNoPlayersMatchCriteria() {
        // Given
        when(playerRepository.findAll()).thenReturn(Arrays.asList(testPlayer1, testPlayer2, testPlayer3));

        // When
        List<Player> result = playerService.getPlayersByName("NonExistentPlayer");

        // Then
        assertThat(result).isEmpty();
        verify(playerRepository).findAll();
    }
}