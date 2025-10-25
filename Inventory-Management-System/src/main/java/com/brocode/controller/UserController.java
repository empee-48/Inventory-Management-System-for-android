package com.brocode.controller;

import com.brocode.entity.User;
import com.brocode.service.UserService;
import com.brocode.service.dto.PasswordChangeDto;
import com.brocode.service.dto.UserCreateDto;
import com.brocode.service.dto.UserEditDto;
import com.brocode.service.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/api/users")
public class UserController {
    private final UserService service;

    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "") String username,
            Authentication authentication
    ){
        String currentUsername = authentication.getName();

        if (!username.isEmpty()){
            Optional<UserResponseDto> user = service.getUser(username);
            if (user.isPresent() && "brocode".equals(user.get().username()) && !"brocode".equals(currentUsername)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(user);
        }

        if (id != null){
            Optional<UserResponseDto> user = service.getUser(id);
            // Filter out brocode unless it's the current user
            if (user.isPresent() && "brocode".equals(user.get().username()) && !"brocode".equals(currentUsername)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(user);
        }

        List<UserResponseDto> users = service.getUsers();
        if (!"brocode".equals(currentUsername)) {
            users = users.stream()
                    .filter(user -> !"brocode".equals(user.username()))
                    .toList();
        }

        return ResponseEntity.ok(users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(
            @RequestBody UserCreateDto dto
    ){
        return service.createUser(dto);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> editUser(
            @RequestBody UserEditDto dto,
            @RequestParam String username
    ){
        if (service.editUser(dto, username)) return ResponseEntity.accepted().build();
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(
            @RequestParam Long id
    ){
        service.delete(id);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrent(Authentication authentication){
        return ResponseEntity.of(service.getUser(authentication.getName()));
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changePassword(
            @RequestBody PasswordChangeDto dto,
            @RequestParam String username
    ){
        service.changePassword(dto, username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/reset-password")
    public void resetPassword(
            @RequestParam String username
    ){
        service.resetPassword(username);
    }

    @PutMapping("/enable")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void enableUser(
            @RequestParam(required = false) Long id
    ){
        service.enableUser(id);
    }
}
