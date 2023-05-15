package com.vbolide.config.annotaions.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public @interface AdminOrUserAccess {

}