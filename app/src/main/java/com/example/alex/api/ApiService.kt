package com.example.alex.api

import com.example.alex.api.models.HealthResponse
import com.example.alex.api.models.HistoryResponse
import com.example.alex.api.models.RegisterRequest
import com.example.alex.api.models.RegisterResponse
import com.example.alex.api.models.UserProfile
import com.example.alex.api.models.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @GET("api/v1/health")
    suspend fun health(): HealthResponse

    @GET("api/v1/users/me/history")
    suspend fun history(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): HistoryResponse

    @DELETE("api/v1/users/me/history")
    suspend fun clearHistory(
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("api/v1/devices/register")
    suspend fun registerDevice(
        @Header("X-Registration-Secret") secret: String,
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @GET("api/v1/users/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): UserProfile

    @PUT("api/v1/users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): UserProfile
}