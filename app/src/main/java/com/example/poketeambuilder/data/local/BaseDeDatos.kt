package com.example.poketeambuilder.data.local

import android.content.Context
import androidx.room.*
import com.example.poketeambuilder.data.model.PokemonTeam

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: PokemonTeam)

    @Query("SELECT * FROM equipos")
    suspend fun getAllTeams(): List<PokemonTeam>

    @Delete
    suspend fun deleteTeam(team: PokemonTeam)

    @Update
    suspend fun updateTeam(team: PokemonTeam)
}

@Database(entities = [PokemonTeam::class], version = 1)
abstract class BaseDeDatos : RoomDatabase() {
    abstract fun teamDao(): TeamDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDeDatos? = null

        fun getDatabase(context: Context): BaseDeDatos {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatos::class.java,
                    "poke_teams_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}