package ru.practicum.explorewithme.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.explorewithme.error.exception.NotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return new ApiError(HttpStatus.CONFLICT, ex.getMessage(),"Data conflict",  timestamp, stackTrace);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidInput(MethodArgumentNotValidException ex) {
        log.warn("400 {}", ex.getMessage());

        String message = Optional
                .ofNullable(ex.getBindingResult().getAllErrors().getFirst())
                .map(ObjectError::getDefaultMessage)
                .orElse(ex.getMessage());

        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return new ApiError(HttpStatus.BAD_REQUEST, message, "Invalid input", timestamp, stackTrace);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException ex) {
        log.warn("404 {}", ex.getMessage());
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), "The required object was not found", timestamp, stackTrace);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception ex) {
        log.error("500 {}", ex.getMessage(), ex);
        String stackTrace = getStackTrace(ex);
        String timestamp = getCurrentTimestamp();
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Internal server error", timestamp, stackTrace);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
}
