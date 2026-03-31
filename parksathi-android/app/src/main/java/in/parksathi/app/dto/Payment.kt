package `in`.parksathi.app.dto

import com.google.gson.annotations.SerializedName

data class PaymentResponse(
    @SerializedName("owner_name") val ownerName: String,
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("parking_id") val parkingId: String,
    @SerializedName("time_stamp") val timeStamp: String, // Represented as String for simpler parsing
    @SerializedName("amount") val amount: Double
)

data class MakePaymentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
