package com.fullstackprojectbackend.securecapita.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class User {


    private Long id;
    @NotEmpty(message="first name cannot be empty")
    private String firstName;
    @NotEmpty(message="last name cannot be empty")
    private String lastName;
    private String phone;
    private String address;
    @NotEmpty(message="email cannot be empty")
    @Email(message = "Invalid email entered. Please enter valid email id")
    private String email;
    @NotEmpty(message="password cannot be empty")
    private String password;
    private Boolean enabled;
    private String title;
    private String bio;
    private String imageUrl;
    private Boolean nonLocked;
    private Boolean mfaEnabled;
    private LocalDateTime createdOn;



}