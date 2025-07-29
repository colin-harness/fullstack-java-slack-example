package com.example.slackchat.controller;

import com.example.slackchat.dto.MessageRequest;
import com.example.slackchat.dto.MessageResponse;
import com.example.slackchat.model.Channel;
import com.example.slackchat.model.Message;
import com.example.slackchat.model.User;
import com.example.slackchat.service.ChannelService;
import com.example.slackchat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<Message>> getMessagesByChannel(@PathVariable Long channelId,
                                                            @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = messageService.findRecentMessagesByChannelId(channelId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/channel/{channelId}/paginated")
    public ResponseEntity<Page<Message>> getMessagesByChannelPaginated(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Optional<Channel> channelOpt = channelService.findById(channelId);
        if (channelOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<Message> messages = messageService.findMessagesByChannelPaginated(channelOpt.get(), page, size);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<?> createMessage(@Valid @RequestBody MessageRequest messageRequest,
                                         Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Channel> channelOpt = channelService.findById(messageRequest.getChannelId());
            
            if (channelOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Channel not found"));
            }

            Channel channel = channelOpt.get();
            
            // Check if user is a member of the channel by comparing user IDs
            boolean isMember = channel.getMembers().stream()
                    .anyMatch(member -> member.getId().equals(user.getId()));
            
            if (!isMember) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: You are not a member of this channel"));
            }

            Message message = messageService.createMessage(messageRequest.getContent(), user, channel);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable Long id,
                                         @Valid @RequestBody MessageRequest messageRequest,
                                         Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Message> messageOpt = messageService.findById(id);
            
            if (messageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Message message = messageOpt.get();
            
            // Check if user is the sender of the message
            if (!message.getSender().getId().equals(user.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: You can only edit your own messages"));
            }

            Message updatedMessage = messageService.updateMessage(id, messageRequest.getContent());
            return ResponseEntity.ok(updatedMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Message> messageOpt = messageService.findById(id);
            
            if (messageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Message message = messageOpt.get();
            
            // Check if user is the sender of the message
            if (!message.getSender().getId().equals(user.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: You can only delete your own messages"));
            }

            messageService.deleteMessage(id);
            return ResponseEntity.ok(new MessageResponse("Message deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
