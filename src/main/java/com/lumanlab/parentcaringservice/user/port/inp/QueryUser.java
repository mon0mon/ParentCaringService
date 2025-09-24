package com.lumanlab.parentcaringservice.user.port.inp;

import com.lumanlab.parentcaringservice.user.domain.User;

public interface QueryUser {
    User findById(Long id);
    User findByEmail(String email);
}
