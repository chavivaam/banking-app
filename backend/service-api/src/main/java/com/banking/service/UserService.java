package com.banking.service;

import com.banking.dto.CreateUserRequest;
import com.banking.dto.UserDTO;
import com.banking.entity.HUser;

import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserRequest request);
    HUser getCurrentUser();
    List<UserDTO> getAllUsers();
}
