package com.epam.finaltask.restcontroller.viewscontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserLoginRequest;
import com.epam.finaltask.dto.request.UserRegistrationRequest;
import com.epam.finaltask.dto.Tokens;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.restcontroller.AuthenticationRestController;
import com.epam.finaltask.service.AuthenticationService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Controller
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthenticationController {
    private final AuthenticationRestController authenticationRestController;
    private final AuthenticationService authenticationService;
    @Qualifier("jwtCookieName")
    private final String jwtCookie;
    @Qualifier("jwtRefreshCookieName")
    private final String jwtRefreshCookie;

    @GetMapping(path = "/sign-in")
    public ModelAndView signIn(@RequestParam(value = "error", required = false) String error) {
        ModelAndView modelAndView = new ModelAndView("auth/sign-in");

        if (error != null) {
            modelAndView.addObject("error", error);
        }

        return modelAndView;
    }
    @GetMapping(path = "/sign-up")
    public ModelAndView signUp() {
        ModelAndView modelAndView = new ModelAndView("auth/sign-up");
        UserRegistrationRequest userRegistrationRequest = new UserRegistrationRequest();

        modelAndView.addObject("user", userRegistrationRequest);
        modelAndView.addObject("roles", Role.values());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@ModelAttribute("user")
                                @Valid UserRegistrationRequest userRegistrationRequest) {
        log.info("Registration request using frontend, from user: {} ",
                userRegistrationRequest.getUsername());
        //register user
        UserDTO userDTO = authenticationRestController.registerUser(userRegistrationRequest);

        //model.addAttribute("user", userDTO);
        log.info("User registered using frontend: {}", userDTO.getUsername());
        return new ModelAndView("auth/sign-in");
    }
    @PostMapping("/login")
    public ModelAndView login(@ModelAttribute("user") @Valid UserLoginRequest userLoginRequest,
                              HttpServletResponse response) {
        log.info("Login request using frontend, from user: {} ",
                userLoginRequest.getUsername());
        //login user
        Tokens tokensResponse = authenticationRestController.login(userLoginRequest);
        //create cookie
        authenticationService.updateCookie(response, new Cookie(jwtCookie, tokensResponse.getJwtToken()), Integer.MAX_VALUE);
        authenticationService.updateCookie(response, new Cookie(jwtRefreshCookie, tokensResponse.getRefreshToken()), Integer.MAX_VALUE);
        //redirect to main
        log.info("Login using frontend successful, from user: {} ", userLoginRequest.getUsername());
        return new ModelAndView("index");
    }
    @PostMapping("/logout")
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout request using frontend");
        //get tokens from cookies
        if (request.getCookies() == null) {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("exception", "You are not logged in");

            return modelAndView;
        }
        //show token
        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> jwtCookie.equals(cookie.getName()))
                .findFirst().map(Cookie::getValue).orElse("null");
        //delete refreshToken from db
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> jwtRefreshCookie.equals(cookie.getName()))
                .findFirst().map(Cookie::getValue).orElse("null");
        //logout user
        authenticationRestController.logout(Tokens.builder()
                .jwtToken(token)
                .refreshToken(refreshToken)
                .build());
        //remove cookies
        authenticationService.updateCookie(response, new Cookie(jwtCookie, null), 0);
        authenticationService.updateCookie(response, new Cookie(jwtRefreshCookie, null), 0);
        //redirect to main
        log.info("Logout using frontend successful");
        return new ModelAndView("index");
    }
}
