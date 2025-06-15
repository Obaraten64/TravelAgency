package com.epam.finaltask.exception;

import com.epam.finaltask.exception.exceptions.RegistrationException;
import com.epam.finaltask.exception.exceptions.UserSearchException;

import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.exception.exceptions.VoucherSearchException;

import lombok.extern.log4j.Log4j2;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice(annotations = RestController.class)
@Log4j2
public class GlobalExceptionHandler {
    //validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleArgumentNotValid(MethodArgumentNotValidException exception) {
        log.error(exception);
        Optional<String> message = Optional.ofNullable(
                exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        return getMap(message.orElse(exception.getMessage()));
    }
    //username not found exception
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUsernameNotFound(UsernameNotFoundException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }
    //bad credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadCredentials(BadCredentialsException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }
    //bad property exception for sorting
    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handlePropertyReference(PropertyReferenceException exception) {
        log.error(exception);
        return getMap("Property invalid. Impossible to search");
    }
    //method argument mismatch exception
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.error(exception);
        return getMap("Property " + exception.getPropertyName() + " not found." +
                " Impossible to sort");
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRegistrationException(RegistrationException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }

    @ExceptionHandler(UserSearchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUserSearchException(UserSearchException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }

    @ExceptionHandler(VoucherSearchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleVoucherSearchException(VoucherSearchException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }

    @ExceptionHandler(VoucherException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleVoucherException(VoucherException exception) {
        log.error(exception);
        return getMap(exception.getMessage());
    }

    private Map<String, String> getMap(String exceptionMessage) {
        return Map.of("exception", exceptionMessage);
    }
}
