package `in`.parksathi.partner.api

import `in`.parksathi.partner.dto.AcquireBookingResponse
import `in`.parksathi.partner.dto.CreateUserResponse
import `in`.parksathi.partner.dto.OwnerBookingResponse
import `in`.parksathi.partner.dto.ParkingResponse
import `in`.parksathi.partner.dto.VerifyRoleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("user/create")
    suspend fun createUser(
        @Header("Authorization") token: String
    ): Response<CreateUserResponse>

    @GET("auth/verify-role")
    suspend fun verifyRole(
        @Header("Authorization") token: String
    ): Response<VerifyRoleResponse>

    @POST("bookings/acquire/{booking_id}")
    suspend fun acquireBooking(
        @Path("booking_id") bookingId: String,
        @Header("Authorization") token: String
    ): Response<AcquireBookingResponse>

    @GET("bookings/owner")
    suspend fun getOwnerBookings(
        @Header("Authorization") token: String
    ): Response<List<OwnerBookingResponse>>

    @Multipart
    @POST("owner/create")
    suspend fun submitParkingDetails(
        @Part("parking_name") parkingName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("id_proof") idProof: RequestBody,
        @Part("slots") slots: RequestBody,
        @Part("hourly_rate") hourlyRate: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part file: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<ParkingResponse>
}
