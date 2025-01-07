package com.backend.exception;


import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle request data
     *
     * @param e
     * @param request
     * @return error
     */
    @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    @ApiResponses(value ={
            @ApiResponse(responseCode = "400", description = "Bad request",
                content = {
                    @Content(mediaType =  MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            name = "Handle exception when data invalid. (@RequestBody, @RequestParam, @PathVariable)",
                            summary = "Handle Bad Request",
                            value = """
                                        {
                                            "timestamp": "2024-04-07T11:38:56.368+00:00",
                                            "status": 400,
                                            "path": "/api/v1/...",
                                            "error": "Invalid Payload",
                                            "message": "{data} must be not blank"
                                        }
                                    """
                    ))
                })

    })
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        String error = "Invalid Data";
        String detailedMessage = message;

        if (e instanceof ConstraintViolationException) {
            error = "Invalid Parameter";
            detailedMessage = message.substring(message.indexOf(" ") + 1);
        } else if (e instanceof MissingServletRequestParameterException) {
            error = "Invalid Parameter";
        } else if (e instanceof MethodArgumentNotValidException) {
            int start = message.lastIndexOf("[") + 1;
            int end = message.lastIndexOf("]") - 1;
            detailedMessage = message.substring(start, end);
            error = "Invalid Payload";
        }

        errorResponse.setError(error);
        errorResponse.setMessage(detailedMessage);

        return errorResponse;
    }

    /**
     * Handle exception when the request not found data
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({ForBiddenException.class, AccessDeniedException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Access denied",
                content =  {
                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                                @ExampleObject(
                                        name = "403 Response",
                                        summary = "Handle exception when access forbidden",
                                        value = """
                                                {
                                                    "timestamp": "2023-10-19T06:07:35.321+00:00",
                                                    "status": 403,
                                                    "path": "/api/v1/...",
                                                    "error": "Forbidden",
                                                    "message": "{data} not found"
                                                }
                                                """
                                )
                        })

                }
            )
    })
    public ErrorResponse handleAccessDeniedException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    /**
     * Handle exception when the request not found data
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                                @ExampleObject(
                                        name = "404 Not Found",
                                        summary = "Handle exception when resource not found",
                                        value = """
                                                {
                                                    "timestamp": "2023-10-19T06:07:35.321+00:00",
                                                    "status": 404,
                                                    "path": "/api/v1/...",
                                                    "error": "Not Found",
                                                    "message": "{data} not found"
                                                }
                                                """
                                )
                        })

            })
    })
    public  ErrorResponse handleResourceNotFoundException(Exception e, WebRequest request){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;

    }

    /**
     * Handle exception when the data is conflicted
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(InvalidDataException.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "409 Response",
                                    summary = "Handle exception when input data is conflicted",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 409,
                                              "path": "/api/v1/...",
                                              "error": "Conflict",
                                              "message": "{data} exists, Please try again!"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleDuplicateKeyException(InvalidDataException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        errorResponse.setError(HttpStatus.CONFLICT.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }

    /**
     * Handle exception when internal server error
     *
     * @param e
     * @param request
     * @return error
     */
    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "500 Response",
                                    summary = "Handle exception when internal server error",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:35:52.333+00:00",
                                              "status": 500,
                                              "path": "/api/v1/...",
                                              "error": "Internal Server Error",
                                              "message": "Connection timeout, please try again"
                                            }
                                            """
                            ))})
    })
    public ErrorResponse handleException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());

        return errorResponse;
    }




    @Getter
    @Setter
    private class ErrorResponse {
        private Date timestamp;
        private int status;
        private String path;
        private String error;
        private String message;
    }
}
