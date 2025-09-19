package com.lumanlab.parentcaringservice.user.application.service;

import com.lumanlab.parentcaringservice.user.domain.User;
import com.lumanlab.parentcaringservice.user.domain.UserRole;
import com.lumanlab.parentcaringservice.user.port.inp.QueryUser;
import com.lumanlab.parentcaringservice.user.port.inp.UpdateUser;
import com.lumanlab.parentcaringservice.user.port.outp.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

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
    public void register(String email, String password, Collection<UserRole> roles) {
        User user = new User(email, password, roles);

        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, String password) {
        User user = findById(userId);

        user.updatePassword(password);
    }

    @Override
    public void withdraw(Long userId) {
        User user = findById(userId);

        user.withdraw();
    }

    @Override
    public void updateTotp(Long userId, String totpSecret) {
        User user = findById(userId);

        user.updateTotpSecret(totpSecret);
    }

    @Override
    public void clearTotp(Long userId) {
        User user = findById(userId);

        user.clearTotpSecret();
    }
}
