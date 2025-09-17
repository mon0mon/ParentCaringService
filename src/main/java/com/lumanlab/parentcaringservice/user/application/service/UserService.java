package com.lumanlab.parentcaringservice.user.application.service;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import com.lumanlab.parentcaringservice.user.port.inp.UpdateUser;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements QueryUser, UpdateUser {

    private final UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Override
    public void register(String email, String password, UserRole role) {
        var user = new User(email, password, role);

        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, String password) {
        var user = findById(userId);

        user.updatePassword(password);
    }

    @Override
    public void updateMfaEnabled(Long userId, boolean mfaEnabled) {
        var user = findById(userId);

        user.updateMfaEnabled(mfaEnabled);
    }

    @Override
    public void withdraw(Long userId) {
        var user = findById(userId);

        user.withdraw();
    }
}
