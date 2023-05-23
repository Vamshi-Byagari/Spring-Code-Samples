package com.vbolide.service;

import com.vbolide.model.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

@Slf4j
@Service
public class PrincipalService {

    public CustomUserDetails getCustomUserDetails(Principal principal){
        if(Objects.isNull(principal)){
            return null;
        }
        if(principal instanceof Authentication){
            Authentication authenticationToken = (Authentication) principal;
            Object tokenPrincipal = authenticationToken.getPrincipal();
            if (tokenPrincipal instanceof CustomUserDetails){
                return (CustomUserDetails) tokenPrincipal;
            }
        }
        if (principal instanceof CustomUserDetails){
            return (CustomUserDetails) principal;
        }
        return null;
    }

}