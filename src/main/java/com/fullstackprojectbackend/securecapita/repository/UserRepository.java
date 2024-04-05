package com.fullstackprojectbackend.securecapita.repository;

import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.form.UpdateForm;

import java.util.Collection;

public interface UserRepository <T extends User>{
    // crud operations

    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    Boolean delete(Long id);
    T update(T data);

    User getUserByEmail(String email);

    void sendVerficationCode(UserDTO userDto);

    T verifyCode(String email, String code);

    void resetPassword(String email);

    T verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    T verifyAccountKey(String key);

    User updateUserDetails(UpdateForm user);
}
