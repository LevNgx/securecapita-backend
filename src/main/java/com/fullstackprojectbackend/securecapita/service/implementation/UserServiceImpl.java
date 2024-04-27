package com.fullstackprojectbackend.securecapita.service.implementation;

import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.dtomapper.UserDTOMapper;
import com.fullstackprojectbackend.securecapita.form.UpdateForm;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.repository.UserRepository;
import com.fullstackprojectbackend.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.fullstackprojectbackend.securecapita.dtomapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    private final RoleRepository roleRepository;
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
       return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerficationCode(UserDTO userDto) {
        userRepository.sendVerficationCode(userDto);

    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTOMapper.fromUser(user, roleRepository.getRoleByUserId(user.getId()));
    }



    @Override
    public User getUser(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepository.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDTO(userRepository.verifyAccountKey(key));
    }

    @Override
    public UserDTO updateUserDetails(UpdateForm user) {
        return mapToUserDTO(userRepository.updateUserDetails(user));
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return mapToUserDTO(userRepository.get(userId));
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        userRepository.updatePassword(id, currentPassword, newPassword, confirmNewPassword);
    }

    @Override
    public void updateUserRole(Long id, String roleName) {
        roleRepository.updateUserRole(id, roleName);
    }

    @Override
    public void updateAccountSettings(Long id, Boolean enabled, Boolean notLocked) {
        userRepository.updateAccountSettings( id,  enabled,  notLocked);
    }

    @Override
    public UserDTO toggleMfa(String email) {
        return mapToUserDTO(userRepository.toggleMfa( email));
    }

    @Override
    public void updateImage(UserDTO userDTO, MultipartFile image) {
        userRepository.updateImage(userDTO, image);
    }
}