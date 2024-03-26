package com.fullstackprojectbackend.securecapita.service.implementation;

import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.dtomapper.UserDTOMapper;
import com.fullstackprojectbackend.securecapita.repository.UserRepository;
import com.fullstackprojectbackend.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.fullstackprojectbackend.securecapita.dtomapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    @Override
    public UserDTO createUser(User user) {
        return fromUser(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
       return fromUser(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerficationCode(UserDTO userDto) {
        userRepository.sendVerficationCode(userDto);

    }

    @Override
    public User getUser(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return fromUser(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return fromUser(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepository.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return fromUser(userRepository.verifyAccountKey(key));
    }
}