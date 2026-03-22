package `in`.parksathi.partner.api

import `in`.parksathi.partner.dto.ApiResponse
import `in`.parksathi.partner.dto.VerifyRoleRequest
import `in`.parksathi.partner.dto.VerifyRoleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/verify-role")
    suspend fun verifyRole(
        @Body request: VerifyRoleRequest
    ): Response<VerifyRoleResponse>

    @Multipart
    @POST("owner/details")
    suspend fun submitOwnerDetails(
        @Part("parkingName") parkingName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("idProof") idProof: RequestBody,
        @Part licenseProof: MultipartBody.Part
    ): Response<ApiResponse>
}
