package com.fullstackprojectbackend.securecapita.repository;

import com.fullstackprojectbackend.securecapita.domain.Role;

import java.util.Collection;

public interface RoleRepository <T extends Role>{

    // crud operations
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    Boolean delete(Long id);
    T update(T data);

    //complexoperations
    void addRoleToUser(long userId, String roleName);
    Role getRoleByUserId(long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);

}