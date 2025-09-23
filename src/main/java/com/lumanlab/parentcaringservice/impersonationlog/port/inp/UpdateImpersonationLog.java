package com.lumanlab.parentcaringservice.impersonationlog.port.inp;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationType;

public interface UpdateImpersonationLog {
    void register(Long adminUserId, Long targetUserId, String ip, ImpersonationType type, String actionDetails);
}
