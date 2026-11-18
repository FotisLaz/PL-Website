package com.example.dataprocessor.repository;

import com.example.dataprocessor.entity.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

// In-memory H2 database; the schema is created from the entity for this test only
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    private Player player(String name, String team) {
        Player p = new Player();
        p.setName(name);
        p.setTeam(team);
        return p;
    }

    @Test
    void findByIdUsesThePlayerName() {
        playerRepository.save(player("Bukayo Saka", "Arsenal"));

        // The @Id is the player name (a String)
        assertThat(playerRepository.findById("Bukayo Saka"))
                .get()
                .extracting(Player::getTeam)
                .isEqualTo("Arsenal");
        assertThat(playerRepository.existsById("Unknown Player")).isFalse();
    }

    @Test
    void findByNameAndTeamMatchesBothFields() {
        playerRepository.save(player("Bukayo Saka", "Arsenal"));

        assertThat(playerRepository.findByNameAndTeam("Bukayo Saka", "Arsenal")).isNotNull();
        assertThat(playerRepository.findByNameAndTeam("Bukayo Saka", "Chelsea")).isNull();
    }
}
