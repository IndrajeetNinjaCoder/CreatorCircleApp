package com.cc.creatorcircleapp.data.repository

import com.cc.creatorcircleapp.data.api.RetrofitInstance
import com.cc.creatorcircleapp.data.models.SignUpRequest
import com.cc.creatorcircleapp.data.models.SignUpResponse
import retrofit2.Response

class AuthRepository {
    suspend fun registerUser(request: SignUpRequest): Response<SignUpResponse> {
        return RetrofitInstance.api.registerUser(request)
    }
}
