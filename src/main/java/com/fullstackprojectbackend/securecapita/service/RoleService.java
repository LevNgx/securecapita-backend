package com.fullstackprojectbackend.securecapita.service;

import com.fullstackprojectbackend.securecapita.domain.Role;

public interface RoleService{

    Role getRoleByUserId(Long id);
}
