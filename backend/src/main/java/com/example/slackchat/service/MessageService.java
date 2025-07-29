package com.example.slackchat.service;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.Message;
import com.example.slackchat.model.User;
import com.example.slackchat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message createMessage(String content, User sender, Channel channel) {
        Message message = new Message(content, sender, channel);
        return messageRepository.save(message);
    }

    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    public List<Message> findMessagesByChannel(Channel channel) {
        return messageRepository.findByChannelOrderByCreatedAtAsc(channel);
    }

    public Page<Message> findMessagesByChannelPaginated(Channel channel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChannelOrderByCreatedAtDesc(channel, pageable);
    }

    public List<Message> findRecentMessagesByChannelId(Long channelId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findRecentMessagesByChannelId(channelId, pageable);
    }

    public Message updateMessage(Long messageId, String newContent) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            message.setContent(newContent);
            return messageRepository.save(message);
        }
        throw new RuntimeException("Message not found");
    }

    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }
}
