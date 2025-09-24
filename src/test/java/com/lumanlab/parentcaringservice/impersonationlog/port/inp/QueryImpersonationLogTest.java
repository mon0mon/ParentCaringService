package com.lumanlab.parentcaringservice.impersonationlog.port.inp;

import com.lumanlab.parentcaringservice.impersonationlog.application.service.ImpersonationLogService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QueryImpersonationLogTest extends BaseUsecaseTest {

    @Autowired
    private ImpersonationLogService impersonationLogService;

    @Autowired
    private ImpersonationLogRepository impersonationLogRepository;

    @Autowired
    private UserRepository userRepository;

    private final String IP = "127.0.0.1";
    private User adminUser;
    private User targetUser1;
    private User targetUser2;
    private List<ImpersonationLog> impersonationLogs = new ArrayList<>();
    private List<ImpersonationLog> filterAdmin = new ArrayList<>();
    private List<ImpersonationLog> filterTargetUser1 = new ArrayList<>();
    private List<ImpersonationLog> filterTargetUser2 = new ArrayList<>();

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(new User("admin@example.com", "password", Set.of(UserRole.ADMIN)));
        targetUser1 = userRepository.save(new User("user1@example.com", "password", Set.of(UserRole.PARENT)));
        targetUser2 = userRepository.save(new User("user2@example.com", "password", Set.of(UserRole.PARENT)));

        impersonationLogs = impersonationLogRepository.saveAll(
                List.of(
                        new ImpersonationLog(adminUser, targetUser1, IP, ImpersonationType.LOGIN, "START"),
                        new ImpersonationLog(adminUser, targetUser1, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser1, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser1, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser1, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser2, IP, ImpersonationType.LOGIN, "START"),
                        new ImpersonationLog(adminUser, targetUser2, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser2, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser2, IP, ImpersonationType.ACTION, "ACTION"),
                        new ImpersonationLog(adminUser, targetUser2, IP, ImpersonationType.ACTION, "ACTION")
                )
        );

        filterAdmin = impersonationLogs.stream().filter(log -> log.getAdmin().equals(adminUser)).toList();
        filterTargetUser1 = impersonationLogs.stream().filter(log -> log.getTargetUser().equals(targetUser1)).toList();
        filterTargetUser2 = impersonationLogs.stream().filter(log -> log.getTargetUser().equals(targetUser2)).toList();
    }

    @Test
    @DisplayName("임퍼소네이션 로그 - 어드민으로 조회")
    void findAllByAdmin() {
        List<ImpersonationLog> actual = impersonationLogService.findAllByAdmin(adminUser.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(filterAdmin);
    }

    @Test
    @DisplayName("임퍼소네이션 로그 - 대상 유저로 조회 - targetUser1")
    void findAllByTargetUser() {
        List<ImpersonationLog> actual = impersonationLogService.findAllByTargetUser(targetUser1.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(filterTargetUser1);
    }

    @Test
    @DisplayName("임퍼소네이션 로그 - 대상 유저로 조회 - targetUser2")
    void findAllByTargetUser2() {
        List<ImpersonationLog> actual = impersonationLogService.findAllByTargetUser(targetUser2.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(filterTargetUser2);
    }

    @Test
    @DisplayName("임퍼소네이션 로그 - 어드민과 대상 유저로 조회 - adminUser, targetUser1")
    void findAllByAdminAndTargetUser1() {
        List<ImpersonationLog> actual = impersonationLogService.findAllByAdminAndTargetUser(adminUser.getId(), targetUser1.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(filterTargetUser1);
    }

    @Test
    @DisplayName("임퍼소네이션 로그 - 어드민과 대상 유저로 조회 - adminUser, targetUser2")
    void findAllByAdminAndTargetUser2() {
        List<ImpersonationLog> actual = impersonationLogService.findAllByAdminAndTargetUser(adminUser.getId(), targetUser2.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(filterTargetUser2);
    }
}
