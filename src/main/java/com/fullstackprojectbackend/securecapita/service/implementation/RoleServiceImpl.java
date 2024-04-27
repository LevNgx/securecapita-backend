package com.fullstackprojectbackend.securecapita.service.implementation;

import com.fullstackprojectbackend.securecapita.domain.Role;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepository.getRoleByUserId(id);
    }

    @Override
    public Collection<Role> getRoles() {
       return roleRepository.list();
    }
}
