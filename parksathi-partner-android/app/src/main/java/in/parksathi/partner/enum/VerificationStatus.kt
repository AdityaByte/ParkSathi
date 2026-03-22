package `in`.parksathi.partner.enum

import com.google.gson.annotations.SerializedName

enum class VerificationStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("approved")
    APPROVED,
    @SerializedName("rejected")
    REJECTED
}