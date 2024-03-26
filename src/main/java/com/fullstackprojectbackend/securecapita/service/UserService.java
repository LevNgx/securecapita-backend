package com.fullstackprojectbackend.securecapita.service;

import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;

public interface UserService {
    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerficationCode(UserDTO userDto);

    User getUser(String email);

    UserDTO verifyCode(String email, String code);

    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccountKey(String key);
}