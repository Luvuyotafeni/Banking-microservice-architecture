package com.banking.usermanagementservice.exception;

public class BeneficiaryNotFoundException extends RuntimeException{
    public BeneficiaryNotFoundException(String message){
        super(message);
    }
}
