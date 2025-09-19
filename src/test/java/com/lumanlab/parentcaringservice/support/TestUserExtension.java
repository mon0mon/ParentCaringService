package com.lumanlab.parentcaringservice.support;

import com.lumanlab.parentcaringservice.support.annotation.WithAdminUser;
import com.lumanlab.parentcaringservice.support.annotation.WithMasterUser;
import com.lumanlab.parentcaringservice.support.annotation.WithParentUser;
import com.lumanlab.parentcaringservice.support.annotation.WithTestUser;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;

public class TestUserExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method testMethod = context.getRequiredTestMethod();
        Object testInstance = context.getRequiredTestInstance();

        // BaseApiTest인지 확인
        if (!(testInstance instanceof BaseApiTest baseTest)) {
            return;
        }

        TestAuthHelper authHelper = getAuthHelper(context);
        String token = null;

        // 어노테이션 확인 및 토큰 생성
        if (testMethod.isAnnotationPresent(WithTestUser.class)) {
            WithTestUser annotation = testMethod.getAnnotation(WithTestUser.class);
            token = authHelper.createUserAndGetToken(
                    annotation.email(),
                    annotation.password(),
                    annotation.totpSecret(),
                    annotation.roles());
        } else if (testMethod.isAnnotationPresent(WithParentUser.class)) {
            WithParentUser annotation = testMethod.getAnnotation(WithParentUser.class);
            token = authHelper.createUserAndGetToken(
                    annotation.email(),
                    annotation.password(),
                    annotation.totpSecret(),
                    UserRole.PARENT);
        } else if (testMethod.isAnnotationPresent(WithAdminUser.class)) {
            WithAdminUser annotation = testMethod.getAnnotation(WithAdminUser.class);
            token = authHelper.createUserAndGetToken(
                    annotation.email(),
                    annotation.password(),
                    annotation.totpSecret(),
                    UserRole.ADMIN);
        } else if (testMethod.isAnnotationPresent(WithMasterUser.class)) {
            WithMasterUser annotation = testMethod.getAnnotation(WithMasterUser.class);
            token = authHelper.createUserAndGetToken(
                    annotation.email(),
                    annotation.password(),
                    annotation.totpSecret(),
                    UserRole.MASTER);
        }

        // 생성된 토큰을 테스트 인스턴스에 저장
        if (token != null) {
            baseTest.setCurrentUserToken(token);
        }
    }

    private TestAuthHelper getAuthHelper(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context)
                .getBean(TestAuthHelper.class);
    }
}
