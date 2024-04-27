package com.fullstackprojectbackend.securecapita.enumeration;

public enum EventType {
    LOGIN_ATTEMPT("You tried to log in "),
    LOGIN_ATTEMPT_FAILURE("Login attempted and failed"),
    LOGIN_ATTEMPT_SUCCESS("Login attempted and succeeded"),
    PROFILE_UPDATE("You updated your profile information"),
    PROFILE_PICTURE_UPDATE("Profile picture updated"),
    ROLE_UPDATE("you updated the role"),
    ACCOUNT_SETTINGS_UPDATE("you updated your account settings"),
    PASSWORD_UPDATE("you updated your password"),
    MFA_UPDATE("You updated your MFA settings");

    private final String description ;

    EventType(String msg){
        this.description = msg;
    }

    private String getDescription(){
        return this.description;
    }


}
