package com.lumanlab.parentcaringservice.user.port.inp.web.view.res;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.domain.UserStatus;

import java.util.List;

public record GetUserProfileViewRes(Long userId, String email, UserStatus status, List<UserRole> role,
                                    Boolean mfaEnabled) {
    public GetUserProfileViewRes(User user) {
        this(user.getId(), user.getEmail(), user.getStatus(), user.getRoles().stream().toList(),
                user.getMfaEnabled());
    }
}
