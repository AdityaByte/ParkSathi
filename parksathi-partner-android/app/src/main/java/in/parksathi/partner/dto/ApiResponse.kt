package `in`.parksathi.partner.dto

import com.google.gson.annotations.SerializedName
import `in`.parksathi.partner.enum.BookingStatus
import `in`.parksathi.partner.enum.VerificationStatus

data class ParkingResponse(
    @SerializedName("id") val id: String,
    @SerializedName("parking_name") val parkingName: String,
    @SerializedName("verification_file_url") val verificationFileURL: String,
    @SerializedName("verification_status") val verificationStatus: VerificationStatus? = null
)

data class CreateUserResponse(
    @SerializedName("message") val message: String
)

data class AcquireBookingResponse(
    @SerializedName("message") val message: String
)

data class OwnerBookingResponse(
    @SerializedName("user_name") val userName: String,
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("booking_status") val bookingStatus: BookingStatus,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("acquired_at") val acquiredAt: String?,
    @SerializedName("completed_at") val completedAt: String?
)