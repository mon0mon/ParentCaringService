package com.lumanlab.parentcaringservice.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithAdminUser {
    String email() default "admin@example.com";

    String password() default "admin123";

    String totpSecret() default "TOTP_SECRET";
}
