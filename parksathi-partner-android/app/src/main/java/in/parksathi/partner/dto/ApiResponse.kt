package `in`.parksathi.partner.dto

import com.google.gson.annotations.SerializedName
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