package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

	@Pattern(regexp="^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
			message = "Bad id!")
	private String id;
	@NotNull
	private String username;
	@NotNull
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
			message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and be between 8 and 20 characters")
	private String password;

	private String role;

	private List<Voucher> vouchers;

	private String phoneNumber;
	@NotNull(message = "Your balance can't be empty")
	@Min(value = 1, message = "Your balance can't be less than 1")
	private Double balance;

	private boolean active;
}
