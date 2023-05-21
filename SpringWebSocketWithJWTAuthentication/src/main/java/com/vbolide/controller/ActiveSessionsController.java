package com.vbolide.controller;

import com.vbolide.config.annotaions.access.AdminAccess;
import com.vbolide.service.PrincipalService;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@AdminAccess
@RequestMapping("/api")
@RestController
public class ActiveSessionsController {

    //private final PrincipalService  principalService;
    private final SimpUserRegistry simpUserRegistry;

    public ActiveSessionsController(PrincipalService principalService, SimpUserRegistry simpUserRegistry){
        //this.principalService = principalService;
        this.simpUserRegistry = simpUserRegistry;
    }

    @GetMapping("/active-sessions")
    public List<String> activeSessions(){
        return simpUserRegistry.getUsers().stream().map(SimpUser::getName).collect(Collectors.toList());
    }

}