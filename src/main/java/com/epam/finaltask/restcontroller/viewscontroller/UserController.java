package com.epam.finaltask.restcontroller.viewscontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.request.UserChangeStatusRequest;
import com.epam.finaltask.dto.request.UserUpdateRequest;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.restcontroller.UserRestController;
import com.epam.finaltask.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Log4j2
public class UserController {
    private final UserService userService;
    private final UserRestController userRestController;

    @GetMapping(path = "/account")
    public ModelAndView account(@AuthenticationPrincipal UserDetails user) {
        ModelAndView modelAndView = new ModelAndView("/user/account");
        log.info("User {} trying to access his account", user.getUsername());

        UserDTO userDTO = userService.getUserByUsername(user.getUsername());

        log.info("User {} successfully accessed his account", user.getUsername());
        log.info("User's id is {}", userDTO.getId());
        modelAndView.addObject("user", userDTO);
        return modelAndView;
    }
	@GetMapping(path ="/update")
    public ModelAndView updatePage() {
        ModelAndView modelAndView = new ModelAndView("/user/update");
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();

        modelAndView.addObject("user", userUpdateRequest);
        modelAndView.addObject("roles", Role.values());

        return modelAndView;
    }
    @GetMapping(path = "/change/status")
    public ModelAndView changeStatusPage() {
        ModelAndView modelAndView = new ModelAndView("/user/change-status");
        UserChangeStatusRequest userChangeStatusRequest = new UserChangeStatusRequest();

        modelAndView.addObject("changeStatus", userChangeStatusRequest);

        return modelAndView;
    }

    @PostMapping("/update")
    public ModelAndView updateUserWithModel(@ModelAttribute("user")
                                       @Valid UserUpdateRequest userUpdateRequest,
                                   @AuthenticationPrincipal UserDetails user) {
        System.out.println(userUpdateRequest);
        ModelAndView modelAndView = new ModelAndView("redirect:/users/account");
        log.info("User, {}, data update using frontend request from: {} ",
                userUpdateRequest.getUsername(), user.getUsername());

        UserDTO userDTO = userRestController.updateUser(userUpdateRequest, user);

        log.info("User updated: {}, using frontend", userDTO.getUsername());
        modelAndView.addObject("user", userDTO);
        return modelAndView;
    }
    @PostMapping("/change/status")
    public ModelAndView changeStatus(@ModelAttribute("changeStatus")
                                         @Valid UserChangeStatusRequest userChangeStatusRequest,
                                     @AuthenticationPrincipal UserDetails user) {
        ModelAndView modelAndView = new ModelAndView("/user/account");
        log.info("Account status change request using frontend from: {} ", user.getUsername());

        UserDTO userDTO = userRestController.changeStatus(userChangeStatusRequest, user);

        log.info("User status updated: {}, using frontend", userDTO.getUsername());
        modelAndView.addObject("user", userDTO);
        return modelAndView;
    }
}
