package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.user.domain.UserRole;

import java.util.Collection;

public interface UpdateUser {
    void register(String email, String password, Collection<UserRole> roles, String totpSecret);

    void updatePassword(Long userId, String password);

    void withdraw(Long userId);
}
