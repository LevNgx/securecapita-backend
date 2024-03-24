package com.fullstackprojectbackend.securecapita.repository.implementation;

import com.fullstackprojectbackend.securecapita.domain.Role;
import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.repository.UserRepository;
import com.fullstackprojectbackend.securecapita.repository.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.fullstackprojectbackend.securecapita.enumeration.RoleType.ROLE_USER;
import static com.fullstackprojectbackend.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.fullstackprojectbackend.securecapita.query.UserQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepoImpl implements UserRepository<User> {


    private final NamedParameterJdbcTemplate jdbc;

    private final RoleRepository<Role> roleRepository;

    @Override
    public User create(User user) {
        //validating email
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email is already in use");
        //creating user
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource sqlParameterSource = getsqlParameterSouce(user);
            jdbc.update(INSERT_USER_QUERY, sqlParameterSource, keyHolder);
            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            //adding role to user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // creating verification link
            String verificationURL = getVerificationURL(UUID.randomUUID().toString(), ACCOUNT.getType());
            // save the URL
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationURL));
            // send verification url to the user
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationURL, ACCOUNT);
            user.setEnabled(false);
            user.setNonLocked(true);
            //return newly created user
            return user;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("an error occurred !!");
        }

    }


    @Override
    public Collection<User> list(int page, int pageSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User get(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User update(User data) {
        // TODO Auto-generated method stub
        return null;
    }

    private Integer getEmailCount(String email) {

        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getsqlParameterSouce(User user) {

        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", new BCryptPasswordEncoder().encode(user.getPassword()));


    }

    // we should not expose back end url to the front end. we need to send only the front end urls to the user. returning backend url for now
    private String getVerificationURL(String key, String type) {

        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify" + type + "/" + key).toUriString();


    }
}