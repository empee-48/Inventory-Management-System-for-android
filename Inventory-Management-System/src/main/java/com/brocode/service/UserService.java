package com.brocode.service;

import com.brocode.entity.User;
import com.brocode.repo.UserRepo;
import com.brocode.service.dto.PasswordChangeDto;
import com.brocode.service.dto.UserCreateDto;
import com.brocode.service.dto.UserEditDto;
import com.brocode.service.dto.UserResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDto> getUsers(){
        return repo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::userToResponse)
                .toList();
    }

    public Optional<UserResponseDto> getUser(String username){
        return repo.findByUsername(username).map(mapper::userToResponse);
    }

    public Optional<UserResponseDto> getUser(Long id){
        return repo.findById(id).map(mapper::userToResponse);
    }

    public User createUser(UserCreateDto dto){
        return repo.save(mapper.createToUser(dto));
    }

    public boolean editUser(UserEditDto dto, String username){
        Optional<User> userOptional = repo.findByUsername(username);

        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();
        user.setEmail(dto.email());
        user.setEnabled(dto.isEnabled());
        user.setRoles(dto.roles());

        repo.save(user);
        return true;
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("User with ID " + id + " not found");
        }
        repo.deleteById(id);
    }

    public void changePassword(PasswordChangeDto dto, String username) {
        User user = repo.findByUsername(username).orElseThrow(() -> new NoSuchElementException("User Not Found"));

        boolean passwordChangeIsLegal = !passwordEncoder.matches(dto.currentPassword(), user.getPassword());

        if (passwordChangeIsLegal) throw new IllegalArgumentException("Current Password Is Incorrect");

        user.setPassword(passwordEncoder.encode(dto.newPassword()));

        repo.save(user);
    }

    public void resetPassword(String username){
        User user = repo.findByUsername(username).orElseThrow(() -> new NoSuchElementException("User Not Found"));
        user.setPassword(passwordEncoder.encode("123456"));

        repo.save(user);
    }

    public void enableUser(Long id) {
        User user = repo.findById(id).orElseThrow(() -> new NoSuchElementException("User Not Found"));

        user.setEnabled(true);
        repo.save(user);
    }
}