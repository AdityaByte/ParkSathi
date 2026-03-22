package `in`.parksathi.partner.dto

import com.google.gson.annotations.SerializedName

data class VerifyRoleRequest(
    @SerializedName("idToken") val idToken: String
)

data class VerifyRoleResponse(
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String? = null
)