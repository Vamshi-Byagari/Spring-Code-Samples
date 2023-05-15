package com.vbolide.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class User {

	private long id;

	@NotNull
	@NotBlank
	@Size(min = 3, max = 15, message = "firstname must be of 3 to 15 characters only")
	private String firstName;

	@NotNull
	@NotBlank
	@Size(min = 3, max = 15, message = "lastname must be of 3 to 15 characters only")
	private String lastName;

	@NotNull
	@NotBlank
	@Email(message = "invalid mail id")
	private String email;

	@NotNull
	@NotBlank
	@Size(min = 10, max = 10, message = "invalid mobile number, mobile number must be of 10 digits.")
	private String mobile;

	@NotNull
	@NotBlank
	@Size(min = 4, max = 6, message = "chosen gender is not supported.")
	private String gender;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Pattern(regexp = "([a-zA-Z0-9_]{5,8})?", message = "invalid password. password must contain one or more alphanumeric and _ characters, length must be in between 5 to 8 characters.")
	private String password;

	private String roles = "ROLE_USER";

}