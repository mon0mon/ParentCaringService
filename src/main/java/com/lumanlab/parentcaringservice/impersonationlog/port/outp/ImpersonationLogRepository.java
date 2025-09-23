package com.lumanlab.parentcaringservice.impersonationlog.port.outp;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationLog;
import com.lumanlab.parentcaringservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImpersonationLogRepository extends JpaRepository<ImpersonationLog, Long> {

    List<ImpersonationLog> findAllByAdmin(User admin);

    List<ImpersonationLog> findAllByTargetUser(User targetUser);

    List<ImpersonationLog> findAllByAdminAndTargetUser(User admin, User targetUser);
}
