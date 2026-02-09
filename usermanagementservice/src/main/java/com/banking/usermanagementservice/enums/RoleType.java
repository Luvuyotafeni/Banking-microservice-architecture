package com.banking.usermanagementservice.enums;

public enum RoleType {
    SUPER_ADMIN("Super Administrator - Full system access"),
    CUSTOMER("Customer - Standard banking user");

    private final String description;

    RoleType(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
