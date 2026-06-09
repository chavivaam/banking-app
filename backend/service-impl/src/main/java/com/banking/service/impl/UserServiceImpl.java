package com.banking.service.impl;

import com.banking.dto.CreateUserRequest;
import com.banking.dto.UserDTO;
import com.banking.entity.HUser;
import com.banking.repository.UserRepository;
import com.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        HUser user = HUser.builder()
                .username(request.userName())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();
        return toDTO(userRepository.save(user));
    }

    @Override
    public HUser getCurrentUser() {
        return (HUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private UserDTO toDTO(HUser user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }
}
