package com.epam.finaltask.config;

import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.token.JWTFilter;

import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final BruteForceAuthProvider bruteForceAuthProvider;
    private final JWTFilter jwtFilter;
    private final String[] adminAndManagerRoles = {Role.ADMIN.name(), Role.MANAGER.name()};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index").permitAll()
                        //View controller rules
                        .requestMatchers("/auth/**").permitAll()
                        //change user status only allowed for ADMIN
                        .requestMatchers("/users/change/status").hasRole(Role.ADMIN.name())
                        .requestMatchers("/users/**").authenticated()
                        //change voucher statuses allowed for ADMIN and MANAGER
                        .requestMatchers(new MvcRequestMatcher(null, "/vouchers/{voucherID}/status"))
                            .hasAnyRole(adminAndManagerRoles)
                        .requestMatchers(new MvcRequestMatcher(null, "/vouchers/{voucherID}/status/hot"))
                            .hasAnyRole(adminAndManagerRoles)
                        //voucher creation allowed only for ADMIN
                        .requestMatchers("/vouchers/create").hasRole(Role.ADMIN.name())
                        //updating voucher allowed only for ADMIN
                        .requestMatchers(new MvcRequestMatcher(null, "/vouchers/{voucherID}/update"))
                            .hasRole(Role.ADMIN.name())
                        //deleting voucher allowed only for ADMIN
                        .requestMatchers(new MvcRequestMatcher(null, "/vouchers/{voucherID}/delete"))
                            .hasRole(Role.ADMIN.name())
                        .requestMatchers("/vouchers/**").authenticated()
                        //REST controller rules
                        .requestMatchers("/api/auth/**").permitAll()
                        //change user status only allowed for ADMIN
                        .requestMatchers("/api/users/change/status").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/users/**").authenticated()
                        //change voucher statuses allowed for ADMIN and MANAGER
                        .requestMatchers(new MvcRequestMatcher(null, "/api/vouchers/{voucherID}/status"))
                            .hasAnyRole(adminAndManagerRoles)
                        //prevent access denial of access from the underline rules
                        .requestMatchers(HttpMethod.GET, "/api/vouchers/").authenticated()
                        //creation, deletion and update allowed only for ADMIN
                        .requestMatchers(new MvcRequestMatcher(null, "/api/vouchers/{voucherID}"))
                            .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/vouchers/**").authenticated()
                        //swagger stuff
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        //deny all request
                        .anyRequest().denyAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(bruteForceAuthProvider);

        return http.build();
    }
}
