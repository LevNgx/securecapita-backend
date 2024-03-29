package com.fullstackprojectbackend.securecapita.utils;

import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

public class UserUtils {

    public static UserDTO getAuthenticatedUser(Authentication authentication){
        return ((UserDTO) authentication.getPrincipal());
    }

    public static UserDTO getLoggedInUser(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

}
