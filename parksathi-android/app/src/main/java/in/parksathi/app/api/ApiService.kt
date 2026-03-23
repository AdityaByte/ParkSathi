package `in`.parksathi.app.api

import `in`.parksathi.app.dto.CreateUserResponse
import `in`.parksathi.app.dto.NearbyParkingSpot
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("user/create")
    suspend fun createUser(@Header("Authorization") token: String): Response<CreateUserResponse>

    @POST("parking/nearby")
    suspend fun findNearbyParking(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<List<NearbyParkingSpot>>

}