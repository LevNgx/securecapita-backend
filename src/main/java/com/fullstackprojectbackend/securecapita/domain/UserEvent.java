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
public class UserEvent {

    private Long id;
    private String type;
    private String description;
    private String device;
    private String ipAddress;
    private LocalDateTime createdAt;

}
