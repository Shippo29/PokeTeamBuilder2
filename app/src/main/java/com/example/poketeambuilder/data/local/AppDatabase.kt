package com.example.poketeambuilder.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.poketeambuilder.data.model.PokemonTeam

@Database(entities = [PokemonTeam::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poketeam_db"
                ).build()
                INSTANCE = instance
                instance
            }
    }
}