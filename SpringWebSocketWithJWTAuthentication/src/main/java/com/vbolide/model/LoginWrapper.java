package com.vbolide.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoginWrapper {

	@NotNull
	@NotBlank
	@Email(message = "invalid mail id")
	private String email;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Pattern(regexp = "([a-zA-Z0-9_]{5,8})?", message = "invalid password. password must contain one or more alphanumeric and _ characters, length must be in between 5 to 8 characters.")
	private String password;

}