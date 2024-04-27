package com.fullstackprojectbackend.securecapita.service;

import com.fullstackprojectbackend.securecapita.domain.Role;

import java.util.Collection;

public interface RoleService{

    Role getRoleByUserId(Long id);

    Collection<Role> getRoles();
}
