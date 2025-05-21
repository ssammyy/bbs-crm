package com.bbs.bbsapi.Exceptions

import org.apache.coyote.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import kotlin.math.log

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse("Business rule violation", ex.message) // Or a more specific error
        println(ex.printStackTrace().toString())
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse) // Return 409 Conflict
    }

    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(ex: NullPointerException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse("Null value encountered", ex.message)
        println(ex.printStackTrace().toString())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse("Invalid argument", ex.message)
        println(ex.printStackTrace().toString())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleUserNotFoundException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse("User not found", ex.message)
        println(ex.printStackTrace().toString())
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse) // Return 401 Unauthorized
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse("An unexpected error occurred", ex.message)
        println(ex.printStackTrace().toString())
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

data class ErrorResponse(
    val error: String,
    val details: String?
)