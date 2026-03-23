package `in`.parksathi.app.dto

import com.google.gson.annotations.SerializedName

enum class VerificationStatus(val value: String) {
    @SerializedName("pending") PENDING("pending"),
    @SerializedName("approved") APPROVED("approved"),
    @SerializedName("rejected") REJECTED("rejected")
}

data class Coordinates(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class NearbyParkingSpot(
    @SerializedName("uid") val uid: String,
    @SerializedName("parking_name") val parkingName: String,
    @SerializedName("address") val address: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("coordinates") val coordinates: Coordinates,
    @SerializedName("slots") val slots: Int,
    @SerializedName("available_slots") val availableSlots: Int,
    @SerializedName("verification_status") val verificationStatus: VerificationStatus,
    @SerializedName("distance") val distance: Double
)