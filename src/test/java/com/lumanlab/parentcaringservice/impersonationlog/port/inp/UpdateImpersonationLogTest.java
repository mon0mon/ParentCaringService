package com.lumanlab.parentcaringservice.impersonationlog.port.inp;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationLog;
import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationType;
import com.lumanlab.parentcaringservice.impersonationlog.port.outp.ImpersonationLogRepository;
import com.lumanlab.parentcaringservice.support.BaseUsecaseTest;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateImpersonationLogTest extends BaseUsecaseTest {

    @Autowired
    private UpdateImpersonationLog updateImpersonationLog;

    @Autowired
    private ImpersonationLogRepository impersonationLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(new User("admin@example.com", "password", Set.of(UserRole.ADMIN)));
        targetUser = userRepository.save(new User("user@example.com", "password", Set.of(UserRole.PARENT)));
    }

    @Test
    @DisplayName("ImpersonationLog - 등록")
    void register() {
        String ip = "127.0.0.1";
        String details = "details";
        updateImpersonationLog.register(adminUser.getId(), targetUser.getId(), ip, ImpersonationType.LOGIN, details);

        ImpersonationLog actual = impersonationLogRepository.findAllByAdminAndTargetUser(adminUser, targetUser)
                .getFirst();

        assertThat(actual).isNotNull();
        assertThat(actual.getAdmin()).isEqualTo(adminUser);
        assertThat(actual.getTargetUser()).isEqualTo(targetUser);
        assertThat(actual.getIp()).isEqualTo(ip);
        assertThat(actual.getType()).isEqualTo(ImpersonationType.LOGIN);
        assertThat(actual.getActionDetails()).isEqualTo(details);
    }
}
