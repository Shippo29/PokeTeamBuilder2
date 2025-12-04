package com.example.poketeambuilder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "equipos")
data class PokemonTeam(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    // Nombre del equipo: coincide con "nombre" o "nombreEquipo" en llamadas
    @ColumnInfo(name = "nombreEquipo")
    val nombreEquipo: String = "",

    // Lista de pokemons serializada a JSON (string) para simplificar el mapeo Room
    @ColumnInfo(name = "pokemones")
    val pokemones: String = "[]" // lista en JSON para simplificar
) {
	// Compatibilidad: si el código usa nombreEquipo en lugar de nombre
	val nombre: String
		get() = nombreEquipo
}