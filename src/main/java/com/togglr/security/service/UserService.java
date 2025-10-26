package com.togglr.security.service;

import com.togglr.security.entity.User;
import com.togglr.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("User", id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new com.togglr.rest.exception.EntityNotFoundException("User", username));
    }

    public User create(String username, String password, String name, String email, String description, String roles) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .email(email)
                .description(description)
                .roles(roles != null ? roles : "USER")
                .build();

        return userRepository.save(user);
    }

    public User update(Long id, String username, String password, String name, String email, String description, String roles, Boolean enabled) {
        User user = findById(id);

        if ("root".equals(user.getUsername())) {
            throw new com.togglr.rest.exception.BadRequestException("Root user cannot be modified");
        }

        ofNullable(username).ifPresent(user::setUsername);
        ofNullable(password).filter(p -> !p.isEmpty()).map(passwordEncoder::encode).ifPresent(user::setPassword);
        ofNullable(name).ifPresent(user::setName);
        ofNullable(email).ifPresent(user::setEmail);
        ofNullable(description).ifPresent(user::setDescription);
        ofNullable(roles).ifPresent(user::setRoles);
        ofNullable(enabled).ifPresent(user::setEnabled);

        return userRepository.save(user);
    }

    public void delete(Long id) {
        User user = findById(id);

        if ("root".equals(user.getUsername())) {
            throw new com.togglr.rest.exception.BadRequestException("Root user cannot be deleted");
        }

        userRepository.deleteById(id);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new com.togglr.rest.exception.BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}