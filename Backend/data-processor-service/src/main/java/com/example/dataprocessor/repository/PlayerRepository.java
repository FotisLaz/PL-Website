package com.example.dataprocessor.repository;

import com.example.dataprocessor.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p WHERE p.name = :name AND p.team = :team")
    Player findByNameAndTeam(@Param("name") String name, @Param("team") String team);

    @Query("SELECT p FROM Player p WHERE p.name = :name")
    Player findByName(@Param("name") String name);
}