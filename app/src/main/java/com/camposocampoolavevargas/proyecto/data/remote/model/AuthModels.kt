package com.camposocampoolavevargas.proyecto.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email_or_phone") val emailOrPhone: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("birth_date") val birthDate: Long? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("commune") val commune: String? = null,
    @SerializedName("university") val university: String? = null,
    @SerializedName("career") val career: String? = null
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("birth_date") val birthDate: Long? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("commune") val commune: String? = null,
    @SerializedName("university") val university: String? = null,
    @SerializedName("career") val career: String? = null
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: UserDto
)
