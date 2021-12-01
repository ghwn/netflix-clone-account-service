package me.ghwn.netflix.accountservice.exceptionhandler;

import io.jsonwebtoken.JwtException;
import me.ghwn.netflix.accountservice.controller.IndexController;
import me.ghwn.netflix.accountservice.exception.AccountNotFoundException;
import me.ghwn.netflix.accountservice.exception.RefreshTokenNotFoundException;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestControllerAdvice
public class AccountControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return makeErrorResponse(e, HttpStatus.BAD_REQUEST);    // or consider 422 Error UNPROCESSABLE_ENTITY
    }

    @ExceptionHandler
    public ResponseEntity<?> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException e) {
        return makeErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleJwtException(JwtException e) {
        return makeErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return makeErrorResponse(e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Exception e) {
        return makeErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return makeErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleBindException(BindException e) {
        return makeErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFoundException(AccountNotFoundException e) {
        return makeErrorResponse(e, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<?> makeErrorResponse(Exception e, HttpStatus httpStatus) {
        EntityModel<Exception> content = EntityModel.of(e);
        content.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return ResponseEntity.status(httpStatus).body(content);
    }

}
