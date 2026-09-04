package com.ankitt.pokedex.core.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps the outcome of a network call so raw exceptions never cross out of this module.
 * Callers (`core:data` repositories) only ever see one of these three cases.
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String?) : ApiResponse<Nothing>()
    data class Exception(val throwable: Throwable) : ApiResponse<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResponse<T> = try {
    ApiResponse.Success(apiCall())
} catch (httpException: HttpException) {
    ApiResponse.Error(code = httpException.code(), message = httpException.message())
} catch (ioException: IOException) {
    ApiResponse.Exception(ioException)
} catch (throwable: Throwable) {
    ApiResponse.Exception(throwable)
}
