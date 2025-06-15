package com.epam.finaltask.exception;

import com.epam.finaltask.exception.exceptions.RegistrationException;
import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.exception.exceptions.VoucherSearchException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice("com.epam.finaltask.restcontroller.viewscontroller")
@Log4j2
public class GlobalViewExceptionHandler {
    //binder for modelAttribute
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    //validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleArgumentNotValid(MethodArgumentNotValidException exception) {
        log.error(exception);
        Optional<String> message = Optional.ofNullable(
                exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        return getModelAndView(message.orElse(exception.getMessage()));
    }
    //username not found exception
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleUsernameNotFound(UsernameNotFoundException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }
    //bad credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadCredentials(BadCredentialsException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }
    //bad property exception for sorting
    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handlePropertyReference(PropertyReferenceException exception) {
        log.error(exception);
        return getModelAndView("Property " + exception.getPropertyName() + " not found." +
                " Impossible to sort");
    }
    //method argument mismatch exception
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.error(exception);
        return getModelAndView("Property invalid. Impossible to search");
    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleRegistrationException(RegistrationException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }

    @ExceptionHandler(UserSearchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleUserSearchException(UserSearchException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }

    @ExceptionHandler(VoucherSearchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleVoucherSearchException(VoucherSearchException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }

    @ExceptionHandler(VoucherException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleVoucherException(VoucherException exception) {
        log.error(exception);
        return getModelAndView(exception.getMessage());
    }

    private ModelAndView getModelAndView(String exceptionMessage) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("exception", exceptionMessage);

        return modelAndView;
    }
}
