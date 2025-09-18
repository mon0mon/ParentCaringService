package com.lumanlab.parentcaringservice.support.annotation;

import com.lumanlab.parentcaringservice.user.domain.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithTestUser {
    String email() default "test@example.com";

    String password() default "password123";

    UserRole[] roles() default {UserRole.PARENT};
}
