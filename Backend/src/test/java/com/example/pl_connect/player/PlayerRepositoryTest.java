package com.example.pl_connect.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

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
        testPlayer2.setName("Martin Ødegaard");
        testPlayer2.setNation("Norway");
        testPlayer2.setPos("CAM");
        testPlayer2.setAge(25);
        testPlayer2.setMp(14);
        testPlayer2.setStarts(14);
        testPlayer2.setMin(1260.0);
        testPlayer2.setGls(4.0);
        testPlayer2.setAst(6.0);
        testPlayer2.setPk(0.0);
        testPlayer2.setCrdy(1.0);
        testPlayer2.setCrdr(0.0);
        testPlayer2.setXg(3.8);
        testPlayer2.setXag(5.9);
        testPlayer2.setTeam("Arsenal");

        testPlayer3 = new Player();
        testPlayer3.setName("Cole Palmer");
        testPlayer3.setNation("England");
        testPlayer3.setPos("RW");
        testPlayer3.setAge(21);
        testPlayer3.setMp(16);
        testPlayer3.setStarts(16);
        testPlayer3.setMin(1440.0);
        testPlayer3.setGls(7.0);
        testPlayer3.setAst(5.0);
        testPlayer3.setPk(2.0);
        testPlayer3.setCrdy(3.0);
        testPlayer3.setCrdr(0.0);
        testPlayer3.setXg(6.1);
        testPlayer3.setXag(4.8);
        testPlayer3.setTeam("Chelsea");

        entityManager.persistAndFlush(testPlayer1);
        entityManager.persistAndFlush(testPlayer2);
        entityManager.persistAndFlush(testPlayer3);
    }

    @Test
    void shouldFindByName() {
        // When
        Optional<Player> found = playerRepository.findByName("Bukayo Saka");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNation()).isEqualTo("England");
        assertThat(found.get().getTeam()).isEqualTo("Arsenal");
    }

    @Test
    void shouldNotFindByNonExistentName() {
        // When
        Optional<Player> found = playerRepository.findByName("Non Existent Player");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindByTeam() {
        // When
        List<Player> arsenalPlayers = playerRepository.findByTeam("Arsenal");

        // Then
        assertThat(arsenalPlayers).hasSize(2);
        assertThat(arsenalPlayers)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Bukayo Saka", "Martin Ødegaard");
    }

    @Test
    void shouldReturnEmptyListForNonExistentTeam() {
        // When
        List<Player> players = playerRepository.findByTeam("Non Existent Team");

        // Then
        assertThat(players).isEmpty();
    }

    @Test
    void shouldDeleteByName() {
        // Given
        assertThat(playerRepository.findByName("Bukayo Saka")).isPresent();

        // When
        playerRepository.deleteByName("Bukayo Saka");
        entityManager.flush();

        // Then
        assertThat(playerRepository.findByName("Bukayo Saka")).isEmpty();
        assertThat(playerRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldSavePlayer() {
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

        // When
        Player saved = playerRepository.save(newPlayer);

        // Then
        assertThat(saved.getName()).isEqualTo("Declan Rice");
        assertThat(playerRepository.findByName("Declan Rice")).isPresent();
        assertThat(playerRepository.findAll()).hasSize(4);
    }

    @Test
    void shouldFindAllPlayers() {
        // When
        List<Player> allPlayers = playerRepository.findAll();

        // Then
        assertThat(allPlayers).hasSize(3);
        assertThat(allPlayers)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Bukayo Saka", "Martin Ødegaard", "Cole Palmer");
    }
}