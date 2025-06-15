package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.epam.finaltask.mapper.EnumConvertor.*;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public User toUser(UserDTO userDTO) {
        return User.builder()
                .id(Optional.ofNullable(userDTO.getId()).map(UUID::fromString).orElse(null))
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .role(getCorrectEnumType(Role.class, userDTO.getRole()))
                .vouchers(userDTO.getVouchers())
                .balance(Optional.ofNullable(userDTO.getBalance()).map(BigDecimal::valueOf).orElse(null))
                .phoneNumber(userDTO.getPhoneNumber())
                .active(userDTO.isActive())
                .build();
    }

    @Override
    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(Optional.ofNullable(user.getId()).map(UUID::toString).orElse(null))
                .username(user.getUsername())
                .password(user.getPassword())
                .role(getStringValueOfEnumType(user.getRole()))
                .vouchers(user.getVouchers())
                .balance(Optional.ofNullable(user.getBalance()).map(b ->
                        Double.valueOf(b.toString())).orElse(null))
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .build();
    }
}
