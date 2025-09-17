package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.user.domain.UserRole;

public interface UpdateUser {
    void register(String email, String password, UserRole role);
    void updatePassword(Long userId, String password);
    void updateMfaEnabled(Long userId, boolean mfaEnabled);
    void withdraw(Long userId);
}
