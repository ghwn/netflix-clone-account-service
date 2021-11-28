package me.ghwn.netflix.accountservice.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        this("Refresh token not found");
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
