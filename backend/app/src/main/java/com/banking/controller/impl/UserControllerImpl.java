package com.banking.controller.impl;

import com.banking.controller.UserController;
import com.banking.dto.CreateUserRequest;
import com.banking.dto.UserDTO;
import com.banking.entity.HUser;
import com.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    public UserDTO create(CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Override
    public UserDTO me(@AuthenticationPrincipal HUser principal) {
        return new UserDTO(principal.getId(), principal.getUsername(), principal.getRole());
    }
}
