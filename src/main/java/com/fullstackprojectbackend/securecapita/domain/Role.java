package com.fullstackprojectbackend.securecapita.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class Role {
    private Long id;
    @NotEmpty(message="role name cannot be empty")
    private String name;
    @NotEmpty(message="permissions cannot be empty")
    private String permissions;
}
