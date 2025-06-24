package com.example.dataprocessor.service;

import com.example.dataprocessor.entity.Player;
import com.example.dataprocessor.model.PlayerData;
import com.example.dataprocessor.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerDataService {

    private static final Logger log = LoggerFactory.getLogger(PlayerDataService.class);

    @Autowired
    private PlayerRepository playerRepository;

    public void processPlayerData(PlayerData playerData) {
        try {
            log.info("Processing player data for: {}", playerData.getName());

            // Check if player already exists
            Player existingPlayer = playerRepository.findByNameAndTeam(
                    playerData.getName(), playerData.getTeam());

            if (existingPlayer != null) {
                // Update existing player
                updatePlayerFromData(existingPlayer, playerData);
                playerRepository.save(existingPlayer);
                log.info("Updated existing player: {}", playerData.getName());
            } else {
                // Create new player
                Player newPlayer = createPlayerFromData(playerData);
                playerRepository.save(newPlayer);
                log.info("Created new player: {}", playerData.getName());
            }

        } catch (Exception e) {
            log.error("Error processing player data for: {}", playerData.getName(), e);
            throw new RuntimeException("Failed to process player data", e);
        }
    }

    private Player createPlayerFromData(PlayerData playerData) {
        Player player = new Player();
        updatePlayerFromData(player, playerData);
        return player;
    }

    private void updatePlayerFromData(Player player, PlayerData playerData) {
        player.setName(playerData.getName());
        player.setNation(playerData.getNation());
        player.setPos(playerData.getPos());
        player.setAge(playerData.getAge());
        player.setMp(playerData.getMp());
        player.setStarts(playerData.getStarts());
        player.setMin(playerData.getMin());
        player.setGls(playerData.getGls());
        player.setAst(playerData.getAst());
        player.setPk(playerData.getPk());
        player.setCrdy(playerData.getCrdy());
        player.setCrdr(playerData.getCrdr());
        player.setXg(playerData.getXg());
        player.setXa(playerData.getXa());
        player.setTeam(playerData.getTeam());
    }
}