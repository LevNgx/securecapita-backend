package com.fullstackprojectbackend.securecapita.repository.implementation;

import com.fullstackprojectbackend.securecapita.domain.Role;
import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.enumeration.VerificationType;
import com.fullstackprojectbackend.securecapita.repository.RoleRepository;
import com.fullstackprojectbackend.securecapita.repository.UserRepository;
import com.fullstackprojectbackend.securecapita.exception.ApiException;
import com.fullstackprojectbackend.securecapita.repository.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

import static com.fullstackprojectbackend.securecapita.enumeration.RoleType.ROLE_USER;
import static com.fullstackprojectbackend.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.fullstackprojectbackend.securecapita.enumeration.VerificationType.PASSWORD;
import static com.fullstackprojectbackend.securecapita.query.UserQuery.*;
import static java.util.Map.of;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateUtils.addDays;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepoImpl implements UserRepository<User> , UserDetailsService {


    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
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

        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();


    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            log.info("User found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            log.info("email has" + email);
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void sendVerficationCode(UserDTO userDto) {

        String expirationDate = DateFormatUtils.format(addDays(new Date(), 1), DATE_FORMAT);
        String verificationCode = randomAlphabetic(8).toUpperCase();
        log.info("Verification code" + verificationCode);

        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, of("id", userDto.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, of("id", userDto.getId(), "code", verificationCode, "expDate", expirationDate));
//            sendSMS(userDto.getPhone(), "From: SecureCapita \nVerification code\n" + verificationCode);
        }  catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }

    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(email, code)) throw new ApiException("provided code is expired. Please login again");
        try{
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())){
                jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID_AND_CODE, of("userId", userByCode.getId(), "code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid. Please try again");
            }
        }
        catch(EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("no user found with the given data.");

        }
        catch(Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred.");
        }

    }

    @Override
    public void resetPassword(String email) {
        log.info("intermediate"  + " " + email);
        try{

            if(getEmailCount(email.trim().toLowerCase()) <= 0) throw new ApiException("Not able to find a user with email" + email);
            else{;
                String expirationDate = DateFormatUtils.format(addDays(new Date(), 1), DATE_FORMAT);

                User user = getUserByEmail(email);
                log.info("intermediate" + user + " " + expirationDate);
                String verificationUrl = getVerificationURL(UUID.randomUUID().toString(), PASSWORD.getType());
                jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId",user.getId()));
                jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
                log.info("verification url" + verificationUrl + " " +  expirationDate + " " + getEmailCount(email.trim().toLowerCase()) + "email " + email);
            }

        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No user found with the provided verification code.");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new ApiException("This link has expired. Please reset your password again");
        try{
            User user =  jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, of("url", getVerificationURL(key, PASSWORD.getType())), new UserRowMapper());
//            jdbc.update(DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY, of("id", user.getId()));
            return user;
        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("This link has expired. Please reset your password again");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred");
        }


    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword) ) throw new ApiException("Passwords don't match. Please try again");
        if(isResetURLDeleted(key, PASSWORD.getType()) ) throw new ApiException("Reset Url has been expired. Please try resetting the password again");

        try{
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, of("password", new BCryptPasswordEncoder(12).encode(password), "url", getVerificationURL(key, PASSWORD.getType()) ));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, of("url", getVerificationURL(key, PASSWORD.getType())));
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public User verifyAccountKey(String key) {
       try{
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY,  of("url", getVerificationURL(key, ACCOUNT.getType())), new UserRowMapper() );
            jdbc.update(DELETE_USER_ENABLED_QUERY, of("enabled", true, "id", user.getId()));
            return user;
       }
       catch(EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("Unable to find a valid record with provided data");
       }
       catch(Exception e){
           log.error(e.getMessage());
           throw new ApiException("An error occurred. Please try again");
       }
    }

    private boolean isResetURLDeleted(String key, String type) {
        String urlKey = getVerificationURL(key, type);
        Integer count = jdbc.queryForObject(FETCH_VERIFICATION_URL_COUNT_QUERY, of("url", urlKey), Integer.class);
        return count.equals(0);
    }

    private Boolean isLinkExpired(String key, VerificationType password){
        try{
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, of("url", getVerificationURL(key, password.getType())), Boolean.class);
        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("This link has expired. Please reset your password again");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred");
        }
    }

    private boolean isVerificationCodeExpired(String email, String code) {
        try{
            return Boolean.TRUE.equals(jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, of("code", code, "userId", getUserByEmail(email).getId()), Boolean.class));
        }
        catch (EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No user found with the provided verification code.");
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException("An error occurred");
        }
    }
}