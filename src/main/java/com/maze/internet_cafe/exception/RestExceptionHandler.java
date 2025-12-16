package com.maze.internet_cafe.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Log4j2
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(EntityExistsException.class)
    protected ResponseEntity<Object> handleEntityExistException(EntityExistsException ex) {
        return buildResponseEntity(new ApiError(CONFLICT, ex.getMessage(), ex));
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(NonUniqueResultException.class)
    protected ResponseEntity<Object> handleNonUniqueResultException(NonUniqueResultException ex) {
        return buildResponseEntity(new ApiError(CONFLICT, ex.getMessage(), ex));
    }

    @ResponseStatus(NOT_ACCEPTABLE)
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        return buildResponseEntity(new ApiError(CONFLICT, ex.getMessage(), ex));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(new ApiError(UNSUPPORTED_MEDIA_TYPE, builder.substring(0, builder.length() - 2), ex));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ApiError apiError = new ApiError(BAD_REQUEST, "Validation error", ex);
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({com.maze.internet_cafe.exception.EntityNotFoundException.class, jakarta.persistence.EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFound(Exception ex) {
        ApiError apiError = new ApiError(NOT_FOUND, ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(NoResultException.class)
    protected ResponseEntity<Object> handleNoResultException(NoResultException ex) {
        ApiError apiError = new ApiError(NOT_FOUND, ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ShareholderVotedException.class)
    protected ResponseEntity<Object> handleShareholderVoted(ShareholderVotedException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex);
        return buildResponseEntity(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = "Error writing JSON output";
        return buildResponseEntity(new ApiError(INTERNAL_SERVER_ERROR, error, ex));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String error = String.format("Could not find %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    protected ResponseEntity<Object> handleDuplicateKeyException(DuplicateKeyException ex) {
        return buildResponseEntity(new ApiError(CONFLICT, ex));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    protected ResponseEntity<Object> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        ApiError apiError = new ApiError(BAD_REQUEST, error, ex);
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
