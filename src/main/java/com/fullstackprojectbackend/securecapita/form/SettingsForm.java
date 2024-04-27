package com.fullstackprojectbackend.securecapita.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsForm {

    @NotNull(message =  "enabled cannot be null")
    private Boolean enabled;
    @NotNull(message =  "not locked cannot be null")
    private Boolean nonLocked;


}
