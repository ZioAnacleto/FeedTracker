package com.zioanacleto.feedtracker.common.exceptions

import io.ktor.http.HttpStatusCode

open class ApplicationException(message: String, val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError) :
    RuntimeException(message)

class ResourceNotFoundException(resource: String, id: String) :
    ApplicationException("$resource with ID $id not found", HttpStatusCode.NotFound)

class ValidationException(message: String) : ApplicationException(message, HttpStatusCode.BadRequest)

class UnauthorizedException(message: String) : ApplicationException(message, HttpStatusCode.Unauthorized)

class ConflictException(message: String) : ApplicationException(message, HttpStatusCode.Conflict)
