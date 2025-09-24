package com.lumanlab.parentcaringservice.impersonationlog.application.service;

import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationLog;
import com.lumanlab.parentcaringservice.impersonationlog.domain.ImpersonationType;
import com.lumanlab.parentcaringservice.impersonationlog.port.inp.QueryImpersonationLog;
import com.lumanlab.parentcaringservice.impersonationlog.port.inp.UpdateImpersonationLog;
import com.lumanlab.parentcaringservice.impersonationlog.port.outp.ImpersonationLogRepository;
import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ImpersonationLogService implements QueryImpersonationLog, UpdateImpersonationLog {

    private final ImpersonationLogRepository impersonationLogRepository;
    private final QueryUser queryUser;

    @Override
    public void register(Long adminUserId, Long targetUserId, String ip, ImpersonationType type, String actionDetails) {
        User admin = queryUser.findById(adminUserId);
        User targetUser = queryUser.findById(targetUserId);

        ImpersonationLog log = new ImpersonationLog(admin, targetUser, ip, type, actionDetails);
        impersonationLogRepository.save(log);
    }

    @Override
    public List<ImpersonationLog> findAllByAdmin(Long adminUserId) {
        User admin = queryUser.findById(adminUserId);

        return impersonationLogRepository.findAllByAdmin(admin);
    }

    @Override
    public List<ImpersonationLog> findAllByTargetUser(Long targetUserId) {
        User targetUser = queryUser.findById(targetUserId);

        return impersonationLogRepository.findAllByTargetUser(targetUser);
    }

    @Override
    public List<ImpersonationLog> findAllByAdminAndTargetUser(Long adminUserId, Long targetUserId) {
        User admin = queryUser.findById(adminUserId);
        User targetUser = queryUser.findById(targetUserId);

        return impersonationLogRepository.findAllByAdminAndTargetUser(admin, targetUser);
    }
}
