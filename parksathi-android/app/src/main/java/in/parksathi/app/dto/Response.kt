package `in`.parksathi.app.dto

import com.google.gson.annotations.SerializedName

data class CreateUserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)