package com.example.slackchat.controller;

import com.example.slackchat.dto.ChannelRequest;
import com.example.slackchat.dto.MessageResponse;
import com.example.slackchat.model.Channel;
import com.example.slackchat.model.User;
import com.example.slackchat.service.ChannelService;
import com.example.slackchat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Channel>> getAllChannels() {
        List<Channel> channels = channelService.findPublicChannels();
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Channel>> getMyChannels(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Channel> channels = channelService.findChannelsByMember(user);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannelById(@PathVariable Long id) {
        Optional<Channel> channel = channelService.findById(id);
        return channel.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createChannel(@Valid @RequestBody ChannelRequest channelRequest,
                                         Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Channel channel = channelService.createChannel(
                channelRequest.getName(),
                channelRequest.getDescription(),
                user
            );
            return ResponseEntity.ok(channel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinChannel(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Channel channel = channelService.addMemberToChannel(id, user);
            return ResponseEntity.ok(channel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveChannel(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Channel channel = channelService.removeMemberFromChannel(id, user);
            return ResponseEntity.ok(channel);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
