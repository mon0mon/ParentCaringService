package com.lumanlab.parentcaringservice.impersonationlog.port.inp;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationLog;

import java.util.List;

public interface QueryImpersonationLog {
    List<ImpersonationLog> findAllByAdmin(Long adminUserId);

    List<ImpersonationLog> findAllByTargetUser(Long targetUserId);

    List<ImpersonationLog> findAllByAdminAndTargetUser(Long adminUserId, Long targetUserId);
}
