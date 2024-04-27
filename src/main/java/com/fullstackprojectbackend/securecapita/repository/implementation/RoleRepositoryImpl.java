package com.fullstackprojectbackend.securecapita.repository.implementation;

import com.fullstackprojectbackend.securecapita.domain.Role;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.exception.ApiException;
import com.fullstackprojectbackend.securecapita.repository.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

import static com.fullstackprojectbackend.securecapita.enumeration.RoleType.ROLE_USER;
import static com.fullstackprojectbackend.securecapita.query.RoleQuery.*;

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
    public Collection<Role> list() {

        log.info("Fetching all the roles from DB");
        try{
            return jdbc.query(SELECT_ROLES_QUERY,  new RoleRowMapper());
//            jdbc.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", role.getId()));
        }

        catch(Exception e){
            log.error(e.getMessage());
            throw new ApiException("an error occurred !!");
        }
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
        try{
            Role role = jdbc.queryForObject(FETCH_ROLE_ID_FROM_USER_ROLES_BY_USER_ID_QUERY, Map.of("userId",userId), new RoleRowMapper());
            System.out.println(role + "role");
            return role;
        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("not able to find a role by the user id " + userId);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("something went wrong");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
        try{
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName",roleName), new RoleRowMapper());
            jdbc.update(UPDATE_USER_ROLE_QUERY, Map.of("roleId", role.getId(), "userId", userId));


        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No role present with the role name" + roleName  );
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("something went wrong");
        }

    }
}
