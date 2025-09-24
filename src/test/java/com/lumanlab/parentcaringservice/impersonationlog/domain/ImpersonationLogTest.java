package com.lumanlab.parentcaringservice.impersonationlog.domain;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImpersonationLogTest {

    private User adminUser;
    private User targetUser;
    private final String ip = "127.0.0.1";
    private final ImpersonationType type = ImpersonationType.LOGIN;
    private final String actionDetails = "Admin logged in as user";

    @BeforeEach
    void setUp() {
        adminUser = new User("admin@example.com", "password", Set.of(UserRole.ADMIN));
        targetUser = new User("user@example.com", "password", Set.of(UserRole.PARENT));
    }

    @Test
    @DisplayName("ImpersonationLog 생성 성공")
    void createImpersonationLog_success() {
        ImpersonationLog log = new ImpersonationLog(adminUser, targetUser, ip, type, actionDetails);

        assertThat(log).isNotNull();
        assertThat(log.getAdmin()).isEqualTo(adminUser);
        assertThat(log.getTargetUser()).isEqualTo(targetUser);
        assertThat(log.getIp()).isEqualTo(ip);
        assertThat(log.getType()).isEqualTo(type);
        assertThat(log.getActionDetails()).isEqualTo(actionDetails);
    }

    @Test
    @DisplayName("Admin User가 null일 경우 예외 발생")
    void createImpersonationLog_throwsException_whenAdminUserIsNull() {
        assertThatThrownBy(() -> new ImpersonationLog(null, targetUser, ip, type, actionDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Admin user must not be null.");
    }

    @Test
    @DisplayName("Target User가 null일 경우 예외 발생")
    void createImpersonationLog_throwsException_whenTargetUserIsNull() {
        assertThatThrownBy(() -> new ImpersonationLog(adminUser, null, ip, type, actionDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target user must not be null.");
    }

    @Test
    @DisplayName("IP 주소가 null일 경우 예외 발생")
    void createImpersonationLog_throwsException_whenIpIsNull() {
        assertThatThrownBy(() -> new ImpersonationLog(adminUser, targetUser, null, type, actionDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IP address must not be null.");
    }

    @Test
    @DisplayName("ImpersonationType이 null일 경우 예외 발생")
    void createImpersonationLog_throwsException_whenTypeIsNull() {
        assertThatThrownBy(() -> new ImpersonationLog(adminUser, targetUser, ip, null, actionDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Impersonation type must not be null.");
    }

    @Test
    @DisplayName("Action Details가 null일 경우 예외 발생")
    void createImpersonationLog_throwsException_whenActionDetailsIsNull() {
        assertThatThrownBy(() -> new ImpersonationLog(adminUser, targetUser, ip, type, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Action details must not be null.");
    }

    @Test
    @DisplayName("Admin User의 역할이 ADMIN이 아닐 경우 예외 발생")
    void createImpersonationLog_throwsException_whenAdminUserIsNotAdmin() {
        User notAdminUser = new User("notadmin@example.com", "password", Set.of(UserRole.PARENT));

        assertThatThrownBy(() -> new ImpersonationLog(notAdminUser, targetUser, ip, type, actionDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Admin user must be a UserRole.ADMIN.");
    }
}
