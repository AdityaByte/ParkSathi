package `in`.parksathi.partner.enum

import com.google.gson.annotations.SerializedName

enum class BookingStatus {
    @SerializedName("booked")
    BOOKED,
    @SerializedName("acquired")
    ACQUIRED,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("cancelled")
    CANCELLED
}
