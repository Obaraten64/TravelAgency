package com.epam.finaltask.auth;

import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;

import lombok.AllArgsConstructor;

import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        log.info("User {} was found", username);

        return new UserAdapter(user);
    }
}
