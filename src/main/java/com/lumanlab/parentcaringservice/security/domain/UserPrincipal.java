package com.lumanlab.parentcaringservice.security.domain;

import com.lumanlab.parentcaringservice.user.domain.UserRole;

import java.util.Set;

public record UserPrincipal(Long id, Set<UserRole> roles) {
}
