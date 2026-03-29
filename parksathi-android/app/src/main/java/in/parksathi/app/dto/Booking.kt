package `in`.parksathi.app.dto

import com.google.gson.annotations.SerializedName

enum class BookingStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("confirmed") CONFIRMED,
    @SerializedName("cancelled") CANCELLED,
    @SerializedName("completed") COMPLETED,
    @SerializedName("acquired") ACQUIRED
}

data class BookingResponse(
    @SerializedName("uid") val uid: String,
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("parking_id") val parkingId: String,
    @SerializedName("booking_status") val bookingStatus: BookingStatus?,
    @SerializedName("parking_name") val parkingName: String,
    @SerializedName("acquired_at") val acquiredAt: String? = null
)
