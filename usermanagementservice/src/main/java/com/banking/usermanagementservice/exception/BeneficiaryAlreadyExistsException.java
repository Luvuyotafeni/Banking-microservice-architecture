package com.banking.usermanagementservice.exception;

public class BeneficiaryAlreadyExistsException extends RuntimeException{
    public BeneficiaryAlreadyExistsException(String message){
        super(message);
    }
}
