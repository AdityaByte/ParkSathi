package `in`.parksathi.partner.dto

import com.google.gson.annotations.SerializedName
import `in`.parksathi.partner.enum.VerificationStatus

data class VerifyRoleResponse(
    @SerializedName("uid") val uid: String,
    @SerializedName("is_owner") val isOwner: Boolean,
    @SerializedName("verification_status") val verificationStatus: VerificationStatus? = null
)