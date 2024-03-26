package com.fullstackprojectbackend.securecapita.resource;

import com.fullstackprojectbackend.securecapita.domain.HttpResponse;
import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.form.LoginForm;
import com.fullstackprojectbackend.securecapita.provider.TokenProvider;
import com.fullstackprojectbackend.securecapita.exception.ApiException;
import com.fullstackprojectbackend.securecapita.service.RoleService;
import com.fullstackprojectbackend.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.fullstackprojectbackend.securecapita.utils.ExceptionUtil.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        try{
            System.out.println(loginForm.getEmail() + " " + loginForm.getPassword());
            Authentication authentication = authentication(loginForm.getEmail(),loginForm.getPassword());
            UserDTO userDto = getAuthenticatedUser(authentication);
            System.out.println("user DTO" + userDto);
            return userDto.getMfaEnabled() ? sendVerificationCode(userDto) : sendResponse(userDto);

        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException(e.getMessage());
        }

    }

    private UserDTO getAuthenticatedUser(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }


    private Authentication authentication(String email, String password){
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return authentication;
        }
        catch (Exception e){
            processError(httpServletRequest, httpServletResponse, e);
            throw new ApiException(e.getMessage());
        }
    }



    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){

        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user",authentication.getPrincipal()))
                        .message("profile opened")
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

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request){
        log.info(TOKEN_PREFIX + "token prefix");
        if(isHeaderTokenValid(request)){
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO userDto = userService.getUserByEmail(tokenProvider.getSubject(token, request));
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
