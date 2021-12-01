package me.ghwn.netflix.accountservice.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@WithSecurityContext(factory = WithMockAccountContextSecurityContextFactory.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMockAccountContext {

    String email() default "user@example.com";

    String password() default "P@ssw0rd1234";

    String[] roles() default {"USER"};

}
