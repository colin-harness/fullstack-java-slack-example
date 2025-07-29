package com.example.slackchat.service;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.Message;
import com.example.slackchat.model.User;
import com.example.slackchat.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private User testUser;
    private Channel testChannel;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        testChannel = new Channel("general", "General discussion", testUser);
        testChannel.setId(1L);
        
        testMessage = new Message("Hello world", testUser, testChannel);
        testMessage.setId(1L);
    }

    @Test
    void createMessage_ValidData_CreatesMessage() {
        // Given
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Message result = messageService.createMessage("Hello world", testUser, testChannel);

        // Then
        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void findById_MessageExists_ReturnsMessage() {
        // Given
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // When
        Optional<Message> result = messageService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Hello world", result.get().getContent());
    }

    @Test
    void findById_MessageNotExists_ReturnsEmpty() {
        // Given
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Message> result = messageService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findMessagesByChannel_ReturnsMessages() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByChannelOrderByCreatedAtAsc(testChannel)).thenReturn(messages);

        // When
        List<Message> result = messageService.findMessagesByChannel(testChannel);

        // Then
        assertEquals(1, result.size());
        assertEquals("Hello world", result.get(0).getContent());
    }

    @Test
    void findMessagesByChannelPaginated_ReturnsPagedMessages() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        Page<Message> page = new PageImpl<>(messages);
        Pageable pageable = PageRequest.of(0, 20);
        when(messageRepository.findByChannelOrderByCreatedAtDesc(testChannel, pageable)).thenReturn(page);

        // When
        Page<Message> result = messageService.findMessagesByChannelPaginated(testChannel, 0, 20);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Hello world", result.getContent().get(0).getContent());
    }

    @Test
    void updateMessage_ValidMessage_UpdatesContent() {
        // Given
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Message result = messageService.updateMessage(1L, "Updated content");

        // Then
        assertNotNull(result);
        verify(messageRepository).save(testMessage);
        assertEquals("Updated content", testMessage.getContent());
    }

    @Test
    void updateMessage_InvalidMessage_ThrowsException() {
        // Given
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> messageService.updateMessage(999L, "Updated content"));
        
        assertEquals("Message not found", exception.getMessage());
    }

    @Test
    void deleteMessage_ValidId_DeletesMessage() {
        // When
        messageService.deleteMessage(1L);

        // Then
        verify(messageRepository).deleteById(1L);
    }

    @Test
    void findRecentMessagesByChannelId_ReturnsRecentMessages() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        Pageable pageable = PageRequest.of(0, 50);
        when(messageRepository.findRecentMessagesByChannelId(eq(1L), any(Pageable.class))).thenReturn(messages);

        // When
        List<Message> result = messageService.findRecentMessagesByChannelId(1L, 50);

        // Then
        assertEquals(1, result.size());
        assertEquals("Hello world", result.get(0).getContent());
    }
}
