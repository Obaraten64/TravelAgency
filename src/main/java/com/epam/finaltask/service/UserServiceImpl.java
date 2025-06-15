package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.exceptions.RegistrationException;
import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;

import jakarta.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.epam.finaltask.mapper.EnumConvertor.getCorrectEnumType;

@Service
@AllArgsConstructor
@Transactional
@Log4j2
public class UserServiceImpl implements UserService {
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Override
	public UserDTO register(UserDTO userDTO) {
		//check if user exists
        log.info("Looking for user: {}", userDTO.getUsername());
		if (userRepository.findUserByUsername(userDTO.getUsername()).isPresent()) {
			log.info("User {} already exists", userDTO.getUsername());
			throw new RegistrationException("Such a user already exists!");
		}
		//create user
		log.info("Registering user: {}", userDTO.getUsername());
		User user = User.builder()
				.username(userDTO.getUsername())
				.password(passwordEncoder.encode(userDTO.getPassword()))
				.role(getCorrectEnumType(Role.class, userDTO.getRole()))
				.phoneNumber(userDTO.getPhoneNumber())
				.balance(BigDecimal.valueOf(userDTO.getBalance()))
				.active(true)
				.build();
		userRepository.save(user);

		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		//find user who requested update
		User userRequester = findUserByUsername(username);
		if (!username.equals(userDTO.getUsername()) && userRequester.getRole() != Role.ADMIN) {
			log.error("Not admin, {}, tried to update {}", username, userDTO.getUsername());
			throw new BadCredentialsException("You are not allowed to update this user!");
		}
		//search for data
		User user = findUserByUsername(userDTO.getUsername());
		log.info("Found {}'s data", userDTO.getUsername());
		//update data
		User updatedUser = User.builder()
				.id(user.getId())
				.username(user.getUsername())
				.password(passwordEncoder.encode(Optional.ofNullable(userDTO.getPassword())
						.orElse(user.getPassword())))
				.role(getCorrectEnumType(Role.class, Optional.ofNullable(userDTO.getRole())
						.orElse(user.getRole().name())))
				.vouchers(user.getVouchers())
				.phoneNumber(Optional.ofNullable(userDTO.getPhoneNumber())
						.orElse(user.getPhoneNumber()))
				.balance(Optional.ofNullable(userDTO.getBalance())
						.map(BigDecimal::valueOf)
						.orElse(user.getBalance()))
				.active(user.isActive())
				.build();
		userRepository.save(updatedUser);
		log.info("Updated {}'s data", userDTO.getUsername());

		return userMapper.toUserDTO(updatedUser);
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		User userPotential = userMapper.toUser(userDTO);
		User user = findUserById(userPotential.getId());
		log.info("Changing account status for {}", userDTO.getUsername());
		user.setActive(userDTO.isActive());

		userRepository.save(user);

		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		return userMapper.toUserDTO(findUserByUsername(username));
	}

	@Override
	public UserDTO getUserById(UUID id) {
		return userMapper.toUserDTO(findUserById(id));
	}

	private User findUserByUsername(String username) {
		return userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserSearchException("User not found!"));
	}

	private User findUserById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserSearchException("User not found!"));
	}
}
