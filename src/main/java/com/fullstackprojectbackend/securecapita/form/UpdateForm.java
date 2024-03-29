package com.fullstackprojectbackend.securecapita.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateForm {
    @NotNull(message = "ID cannot be empty")
    private Long id;
    @NotEmpty(message="first name cannot be empty")
    private String firstName;
    @NotEmpty(message="last name cannot be empty")
    private String lastName;
    @Pattern(regexp = "^\\d{12}$", message = "Invalid phone number")
    private String phone;
    private String address;
    @NotEmpty(message="email cannot be empty")
    @Email(message = "Invalid email entered. Please enter valid email id")
    private String email;
    private Boolean enabled;
    private String title;
    private String bio;

}
