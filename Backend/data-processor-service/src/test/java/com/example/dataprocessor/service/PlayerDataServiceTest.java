package com.example.dataprocessor.service;

import com.example.dataprocessor.entity.Player;
import com.example.dataprocessor.model.PlayerData;
import com.example.dataprocessor.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerDataServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerDataService playerDataService;

    private PlayerData sampleData() {
        return new PlayerData("Bukayo Saka", "ENG", "FW", 22, 35, 35, 3000.0, 16.0,
                9.0, 3.0, 4.0, 0.0, 14.5, 10.2, "Arsenal");
    }

    @Test
    void newPlayerIsSavedWithAllFields() {
        when(playerRepository.findByNameAndTeam("Bukayo Saka", "Arsenal")).thenReturn(null);

        playerDataService.processPlayerData(sampleData());

        ArgumentCaptor<Player> saved = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(saved.capture());
        Player p = saved.getValue();
        assertThat(p.getName()).isEqualTo("Bukayo Saka");
        assertThat(p.getNation()).isEqualTo("ENG");
        assertThat(p.getPos()).isEqualTo("FW");
        assertThat(p.getAge()).isEqualTo(22);
        assertThat(p.getMp()).isEqualTo(35);
        assertThat(p.getStarts()).isEqualTo(35);
        assertThat(p.getMin()).isEqualTo(3000.0);
        assertThat(p.getGls()).isEqualTo(16.0);
        assertThat(p.getAst()).isEqualTo(9.0);
        assertThat(p.getPk()).isEqualTo(3.0);
        assertThat(p.getCrdy()).isEqualTo(4.0);
        assertThat(p.getCrdr()).isEqualTo(0.0);
        assertThat(p.getXg()).isEqualTo(14.5);
        assertThat(p.getXa()).isEqualTo(10.2);
        assertThat(p.getTeam()).isEqualTo("Arsenal");
    }

    @Test
    void existingPlayerIsUpdatedInPlace() {
        Player existing = new Player();
        existing.setName("Bukayo Saka");
        existing.setTeam("Arsenal");
        existing.setGls(1.0);
        when(playerRepository.findByNameAndTeam("Bukayo Saka", "Arsenal")).thenReturn(existing);

        playerDataService.processPlayerData(sampleData());

        // The same entity object is saved, with the new stats
        verify(playerRepository).save(existing);
        assertThat(existing.getGls()).isEqualTo(16.0);
        assertThat(existing.getXa()).isEqualTo(10.2);
    }

    @Test
    void repositoryErrorIsWrappedInRuntimeException() {
        when(playerRepository.findByNameAndTeam(any(), any()))
                .thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> playerDataService.processPlayerData(sampleData()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to process player data")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
