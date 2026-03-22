package `in`.parksathi.app.api

import `in`.parksathi.app.dto.CreateUserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("user/create")
    suspend fun createUser(@Header("Authorization") token: String): Response<CreateUserResponse>

}
