package com.fullstackprojectbackend.securecapita.resource;

import com.fullstackprojectbackend.securecapita.domain.HttpResponse;
import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.domain.UserEvent;
import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.enumeration.EventType;
import com.fullstackprojectbackend.securecapita.event.NewUserEvent;
import com.fullstackprojectbackend.securecapita.form.LoginForm;
import com.fullstackprojectbackend.securecapita.form.SettingsForm;
import com.fullstackprojectbackend.securecapita.form.UpdateForm;
import com.fullstackprojectbackend.securecapita.form.UpdatePasswordForm;
import com.fullstackprojectbackend.securecapita.provider.TokenProvider;
import com.fullstackprojectbackend.securecapita.exception.ApiException;
import com.fullstackprojectbackend.securecapita.service.RoleService;
import com.fullstackprojectbackend.securecapita.service.UserService;
import com.fullstackprojectbackend.securecapita.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.awt.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.fullstackprojectbackend.securecapita.enumeration.EventType.*;
import static com.fullstackprojectbackend.securecapita.utils.ExceptionUtil.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private static final String TOKEN_PREFIX = "Bearer ";
    private final ApplicationEventPublisher publisher;


    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {

            System.out.println(loginForm.getEmail() + " " + loginForm.getPassword());

            UserDTO userDto =  authentication(loginForm.getEmail(),loginForm.getPassword());
            System.out.println("user DTO" + userDto);
            return userDto.getMfaEnabled() ? sendVerificationCode(userDto) : sendResponse(userDto);



    }

    private UserDTO getAuthenticatedUser(Authentication authentication){
        log.info("inside get authenticated user " + authentication.getPrincipal());
        return (UserDTO) authentication.getPrincipal();
    }


    private UserDTO authentication(String email, String password){
        try{
            if(null != userService.getUserByEmail(email)){
                publisher.publishEvent(new NewUserEvent(email, EventType.LOGIN_ATTEMPT));
            }
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)  );
            return getAuthenticatedUser(authentication);
//            return authentication;
        }
        catch (Exception e){
            publisher.publishEvent(new NewUserEvent(email, EventType.LOGIN_ATTEMPT_FAILURE));
            processError(httpServletRequest, httpServletResponse, e);
            throw new ApiException(e.getMessage());
        }
    }



    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDTO userDTO = userService.getUserByEmail(UserUtils.getAuthenticatedUser(authentication).getEmail());

        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user",authentication.getPrincipal(), "roles", roleService.getRoles()))
                        .message("profile retrieved")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm user){
        UserDTO updatedUser = userService.updateUserDetails(user);
        publisher.publishEvent(new NewUserEvent(updatedUser.getEmail(), PROFILE_UPDATE));
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", updatedUser))
                        .message("User updated")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }
// START - To reset password when user is not logged in
    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email){
        userService.resetPassword(email);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset mail has been sent to your email. Please use it to reset your password")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPassword(@PathVariable("key") String key){
        UserDTO userDTO = userService.verifyPasswordKey(key);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("Please enter your new password")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @PostMapping("/resetpassword/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> resetPasswordWithKey(@PathVariable("key") String key, @PathVariable("password") String password, @PathVariable("confirmPassword") String confirmPassword){
        userService.renewPassword(key, password, confirmPassword);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password has been reset successfully")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }
//    END
    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){
        UserDTO userDTO = userService.createUser(user);
        System.out.println(userDTO);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user",userDTO))
                        .message("user created")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code ){
        UserDTO userDto = userService.verifyCode(email, code);
        publisher.publishEvent(new NewUserEvent(userDto.getEmail(), LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .message("code is valid")
                        .statusCode(HttpStatus.OK.value())
                        .data(Map.of("user",userDto
                                , "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto))
                                , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .build());
    }
    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key){
        return new ResponseEntity(
                HttpResponse.builder()
                        .message(userService.verifyAccountKey(key).getEnabled() ? "Account already verified" : "Account verified")
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form){
        log.info("uwer dto" + form.toString() + " " + form.getNewPassword() + " " + form.getCurrentPassword());
        UserDTO userDTO = getAuthenticatedUser(authentication);
        log.info("uwer dto" + userDTO);
        userService.updatePassword(userDTO.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmNewPassword());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), PASSWORD_UPDATE));
        return new ResponseEntity(
                HttpResponse.builder()
                        .message("Password updated successfully")
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication, @PathVariable String roleName){
//        log.info("uwer dto" + form.toString() + " " + form.getNewPassword() + " " + form.getCurrentPassword());
        UserDTO userDTO = getAuthenticatedUser(authentication);
//        log.info("uwer dto" + userDTO);
        userService.updateUserRole(userDTO.getId(), roleName);
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ROLE_UPDATE));
        return new ResponseEntity(
                HttpResponse.builder()
                        .message("Password updated successfully")
                        .data(of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles()))
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication, @RequestBody @Valid SettingsForm form){
//        log.info("uwer dto" + form.toString() + " " + form.getNewPassword() + " " + form.getCurrentPassword());
        UserDTO userDTO = getAuthenticatedUser(authentication);
//        log.info("uwer dto" + userDTO);
        userService.updateAccountSettings(userDTO.getId(), form.getEnabled(), form.getNonLocked());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ACCOUNT_SETTINGS_UPDATE));
        return new ResponseEntity(
                HttpResponse.builder()
                        .message("Account settings updated successfully")
                        .data(of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles()))
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication){
        UserDTO userDTO = userService.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), MFA_UPDATE));
        return new ResponseEntity(
                HttpResponse.builder()
                        .message("MFA authentication updated")
                        .data(of("user", userDTO, "roles", roleService.getRoles()))
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image ) throws  InterruptedException{
        UserDTO userDTO = getAuthenticatedUser(authentication);
        userService.updateImage(userDTO, image);
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), PROFILE_PICTURE_UPDATE));
        return new ResponseEntity(
                HttpResponse.builder()
                        .message("Image updated successfully")
                        .data(of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles()))
                        .statusCode(HttpStatus.OK.value())
                        .build(), HttpStatus.OK);
    }

    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception{
        log.info(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName).toString());
       return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName));

    }
    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request){
        log.info(TOKEN_PREFIX + "token prefix");
        if(isHeaderTokenValid(request)){
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO userDto = userService.getUserById(tokenProvider.getSubject(token, request));
            return new ResponseEntity(
                    HttpResponse.builder()
                            .message("Token refreshed")
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .data(Map.of("user",userDto
                                    , "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto))
                                    , "refresh_token", token))
                            .build(), HttpStatus.OK);
        }
        else{
            return new ResponseEntity(
                    HttpResponse.builder()
                            .message("Token not refreshed due to some invalid details")
                            .status(HttpStatus.BAD_REQUEST)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .build(), HttpStatus.BAD_REQUEST);
        }

    }

    private boolean isHeaderTokenValid(HttpServletRequest request) {

        System.out.println("token prefix" +  TOKEN_PREFIX);
        return request.getHeader(AUTHORIZATION) != null && request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
                && tokenProvider.isTokenValid(tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()) , request), request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()));
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> error(HttpServletRequest request){
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .reason("The path you are trying to go for " + request.getMethod() + " is not a valid route.")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
    }



    private URI getUri(){
        System.out.println("Uri surgical strike" + URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString()));
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDto) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .message("login attempted")
                        .reason("login attempted")
                        .statusCode(HttpStatus.OK.value())
                        .data(Map.of("user",userDto
                                , "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto))
                                , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDTO userDto) {
        return new UserPrincipal(userService.getUser(userDto.getEmail()), roleService.getRoleByUserId(userDto.getId()));
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDto) {
        userService.sendVerficationCode(userDto);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .message("Verification code sent")
                        .statusCode(HttpStatus.OK.value())
                        .data(Map.of("user",userDto))
                        .build());
    }



}
