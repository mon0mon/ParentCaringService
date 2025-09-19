package com.lumanlab.parentcaringservice.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithParentUser {
    String email() default "parent@example.com";

    String password() default "parent123";

    String totpSecret() default "TOTP_SECRET";
}
