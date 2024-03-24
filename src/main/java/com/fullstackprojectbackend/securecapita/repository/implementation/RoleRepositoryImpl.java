package com.fullstackprojectbackend.securecapita.repository.implementation;

import com.fullstackprojectbackend.securecapita.domain.Role;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.repository.exception.ApiException;
import com.fullstackprojectbackend.securecapita.repository.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.fullstackprojectbackend.securecapita.enumeration.RoleType.ROLE_USER;
import static com.fullstackprojectbackend.securecapita.query.RoleQuery.INSERT_ROLE_TO_USER_QUERY;
import static com.fullstackprojectbackend.securecapita.query.RoleQuery.SELECT_ROLE_BY_NAME_QUERY;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {
    private final NamedParameterJdbcTemplate jdbc;
    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public void addRoleToUser(long userId, String roleName) {
        log.info("Adding role {} to user id : {} ",roleName, userId );
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            jdbc.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", role.getId()));
        }
        catch(EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No role found by name" + ROLE_USER.name());
        }
        catch(Exception e){
            log.error(e.getMessage());
            throw new ApiException("an error occurred !!");
        }
    }

    @Override
    public Role getRoleByUserId(long userId) {
        return null;
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
