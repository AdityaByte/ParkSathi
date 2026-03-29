package `in`.parksathi.app.api

import `in`.parksathi.app.dto.BookingResponse
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

    @POST("bookings/create")
    suspend fun createBooking(
        @Query("parking_id") parkingId: String,
        @Header("Authorization") token: String
    ): Response<BookingResponse>

    @GET("bookings/my")
    suspend fun getMyBookings(
        @Header("Authorization") token: String
    ): Response<List<BookingResponse>>

    @POST("bookings/cancel")
    suspend fun cancelBooking(
        @Query("booking_id") bookingId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

}
