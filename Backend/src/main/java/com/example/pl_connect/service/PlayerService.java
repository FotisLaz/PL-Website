package com.example.pl_connect.service;

import com.example.pl_connect.player.Player;
import com.example.pl_connect.player.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Cacheable(value = "allPlayers")
    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    @Cacheable(value = "playersByTeam", key = "#teamName")
    public List<Player> getPlayersFromTeam(String teamName) {
        return playerRepository.findByTeam(teamName);
    }

    @Cacheable(value = "playersByName", key = "#searchText")
    public List<Player> getPlayersByName(String searchText) {
        return playerRepository.findAll().stream()
                .filter(player -> player.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "playersByPos", key = "#searchText")
    public List<Player> getPlayersByPos(String searchText) {
        return playerRepository.findAll().stream()
                .filter(player -> player.getPos().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "playersByNation", key = "#searchText")
    public List<Player> getPlayersByNation(String searchText) {
        return playerRepository.findAll().stream()
                .filter(player -> player.getNation().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "playersByTeamAndPos", key = "#team + '-' + #position")
    public List<Player> getPlayersByTeamAndPosition(String team, String position) {
        return playerRepository.findAll().stream()
                .filter(player -> team.equals(player.getTeam()) && position.equals(player.getPos()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"allPlayers", "playersByTeam", "playersByName", "playersByPos", "playersByNation", "playersByTeamAndPos"}, allEntries = true)
    public Player addPlayer(Player player) {
        playerRepository.save(player);
        return player;
    }

    @CacheEvict(value = {"allPlayers", "playersByTeam", "playersByName", "playersByPos", "playersByNation", "playersByTeamAndPos"}, allEntries = true)
    public Player updatePlayer(Player updatedPlayer) {
        Optional<Player> existingPlayer = playerRepository.findByName(updatedPlayer.getName());

        if (existingPlayer.isPresent()) {
            Player playerToUpdate = existingPlayer.get();
            playerToUpdate.setName(updatedPlayer.getName());
            playerToUpdate.setTeam(updatedPlayer.getTeam());
            playerToUpdate.setPos(updatedPlayer.getPos());
            playerToUpdate.setNation(updatedPlayer.getNation());
            playerRepository.save(playerToUpdate);
            return playerToUpdate;
        }
        return null;
    }

    @Transactional
    @CacheEvict(value = {"allPlayers", "playersByTeam", "playersByName", "playersByPos", "playersByNation", "playersByTeamAndPos"}, allEntries = true)
    public void deletePlayer(String playerName) {
        playerRepository.deleteByName(playerName);
    }
}
