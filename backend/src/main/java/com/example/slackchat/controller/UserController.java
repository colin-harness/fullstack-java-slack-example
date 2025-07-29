package com.example.slackchat.controller;

import com.example.slackchat.model.User;
import com.example.slackchat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User userUpdate, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Update allowed fields
        if (userUpdate.getDisplayName() != null) {
            currentUser.setDisplayName(userUpdate.getDisplayName());
        }
        if (userUpdate.getBio() != null) {
            currentUser.setBio(userUpdate.getBio());
        }
        
        User updatedUser = userService.updateUser(currentUser);
        return ResponseEntity.ok(updatedUser);
    }
}
