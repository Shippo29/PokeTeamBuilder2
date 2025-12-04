package com.example.poketeambuilder.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.poketeambuilder.data.model.PokemonTeam

@Dao
interface TeamDao {
    @Query("SELECT * FROM equipos")
    fun getAllTeams(): List<PokemonTeam> // toma 0 argumentos

    @Query("SELECT * FROM equipos WHERE id = :id")
    fun getTeamById(id: Int): PokemonTeam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(team: PokemonTeam)

    @Delete
    fun delete(team: PokemonTeam)
}