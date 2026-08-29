package com.devangdayal.flashsale.common.exception;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException() {
        super("Refresh token revoked");
    }

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}