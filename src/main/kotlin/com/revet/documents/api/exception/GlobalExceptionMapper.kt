package com.revet.documents.api.exception

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger

/**
 * Global exception mapper to handle uncaught exceptions and ensure proper error responses.
 * Does not handle WebApplicationException (404, 405, etc.) as those are handled by JAX-RS.
 */
@Provider
class GlobalExceptionMapper : ExceptionMapper<Exception> {

    private val logger = Logger.getLogger(GlobalExceptionMapper::class.java)

    override fun toResponse(exception: Exception): Response {
        // Don't handle WebApplicationException (404, 405, etc.) - let JAX-RS handle those
        if (exception is WebApplicationException) {
            return exception.response
        }

        logger.error("Unhandled exception", exception)

        val message = when (exception) {
            is IllegalArgumentException -> exception.message ?: "Invalid argument"
            is IllegalStateException -> exception.message ?: "Invalid state"
            else -> "An error occurred: ${exception.message}"
        }

        val status = when (exception) {
            is IllegalArgumentException -> Response.Status.BAD_REQUEST
            is IllegalStateException -> Response.Status.CONFLICT
            else -> Response.Status.INTERNAL_SERVER_ERROR
        }

        return Response.status(status)
            .entity(mapOf(
                "error" to message,
                "type" to exception.javaClass.simpleName
            ))
            .build()
    }
}
