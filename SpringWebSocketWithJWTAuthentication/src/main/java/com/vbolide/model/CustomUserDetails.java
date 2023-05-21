package com.vbolide.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@ToString
@Getter
@Component
@Scope("prototype")
public class CustomUserDetails implements UserDetails{

	private static final long serialVersionUID = 2534479541998486550L;

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if(user.getRoles() == null) {
			return Collections.emptyList();
		}
		return AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRoles());
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}