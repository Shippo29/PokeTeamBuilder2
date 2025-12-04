package com.example.poketeambuilder.data.network

import retrofit2.http.*
import retrofit2.Response
import com.example.poketeambuilder.data.model.*

interface ApiService {

    @GET("generation/{id}")
    suspend fun getGeneration(@Path("id") id: Int): GenerationResponse

    @GET("pokemon")
    suspend fun listPokemon(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListResponse

    @GET("pokemon/{nameOrId}")
    suspend fun getPokemon(@Path("nameOrId") nameOrId: String): PokemonResponse

    @GET("pokemon-species/{nameOrId}")
    suspend fun getPokemonSpecies(@Path("nameOrId") nameOrId: String): PokemonSpeciesResponse

    @GET("type")
    suspend fun getTypes(): TypesListResponse


    @GET("teams")
    suspend fun getMyTeams(): List<PokemonTeam>

    @POST("teams")
    suspend fun createTeam(@Body team: PokemonTeam): Response<PokemonTeam>

    @DELETE("teams/{id}")
    suspend fun deleteTeam(@Path("id") id: Int): Response<Void>

    @PUT("teams/{id}")
    suspend fun updateTeam(@Path("id") id: Int, @Body team: PokemonTeam): Response<PokemonTeam>
}

