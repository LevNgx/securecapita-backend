package com.fullstackprojectbackend.securecapita.resource;

import com.fullstackprojectbackend.securecapita.domain.HttpResponse;
import com.fullstackprojectbackend.securecapita.domain.User;
import com.fullstackprojectbackend.securecapita.domain.UserPrincipal;
import com.fullstackprojectbackend.securecapita.dto.UserDTO;
import com.fullstackprojectbackend.securecapita.form.LoginForm;
import com.fullstackprojectbackend.securecapita.provider.TokenProvider;
import com.fullstackprojectbackend.securecapita.repository.exception.ApiException;
import com.fullstackprojectbackend.securecapita.service.RoleService;
import com.fullstackprojectbackend.securecapita.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.Map.of;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        try{
            System.out.println(loginForm.getEmail() + " " + loginForm.getPassword());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(),loginForm.getPassword()));
            System.out.println("authenticated");
            UserDTO userDto = userService.getUserByEmail(loginForm.getEmail());
            System.out.println("user DTO" + userDto);
            return userDto.getMfaEnabled() ? sendVerificationCode(userDto) : sendResponse(userDto);

        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException(e.getMessage());
        }

    }



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
                        .message("login attempted")
                        .reason("login attempted")
                        .statusCode(HttpStatus.OK.value())
                        .data(Map.of("user",userDto
                                , "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto))
                                , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
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
