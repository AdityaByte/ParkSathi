package `in`.parksathi.app.dto

import com.google.gson.annotations.SerializedName

enum class BookingStatus {
    @SerializedName("booked") BOOKED,
    @SerializedName("acquired") ACQUIRED,
    @SerializedName("completed") COMPLETED,
    @SerializedName("cancelled") CANCELLED,

    @SerializedName("expired") EXPIRED

}

data class BookingResponse(
    @SerializedName("uid") val uid: String,
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("parking_id") val parkingId: String,
    @SerializedName("booking_status") val bookingStatus: BookingStatus?,
    @SerializedName("parking_name") val parkingName: String,
    @SerializedName("acquired_at") val acquiredAt: String? = null
)
