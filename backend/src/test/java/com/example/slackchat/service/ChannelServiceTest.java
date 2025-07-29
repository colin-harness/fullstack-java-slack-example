package com.example.slackchat.service;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.User;
import com.example.slackchat.repository.ChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelService channelService;

    private User testUser;
    private Channel testChannel;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        testChannel = new Channel("general", "General discussion", testUser);
        testChannel.setId(1L);
    }

    @Test
    void createChannel_ValidData_CreatesChannel() {
        // Given
        when(channelRepository.existsByName("newchannel")).thenReturn(false);
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        Channel result = channelService.createChannel("newchannel", "New channel", testUser);

        // Then
        assertNotNull(result);
        verify(channelRepository).save(any(Channel.class));
    }

    @Test
    void createChannel_NameExists_ThrowsException() {
        // Given
        when(channelRepository.existsByName("existing")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> channelService.createChannel("existing", "Description", testUser));
        
        assertEquals("Channel name already exists", exception.getMessage());
        verify(channelRepository, never()).save(any(Channel.class));
    }

    @Test
    void findById_ChannelExists_ReturnsChannel() {
        // Given
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));

        // When
        Optional<Channel> result = channelService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("general", result.get().getName());
    }

    @Test
    void findById_ChannelNotExists_ReturnsEmpty() {
        // Given
        when(channelRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Channel> result = channelService.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findPublicChannels_ReturnsPublicChannels() {
        // Given
        List<Channel> publicChannels = Arrays.asList(testChannel);
        when(channelRepository.findPublicChannels()).thenReturn(publicChannels);

        // When
        List<Channel> result = channelService.findPublicChannels();

        // Then
        assertEquals(1, result.size());
        assertEquals("general", result.get(0).getName());
    }

    @Test
    void addMemberToChannel_ValidChannel_AddsMember() {
        // Given
        User newMember = new User("newuser", "new@example.com", "password");
        newMember.setId(2L);
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        Channel result = channelService.addMemberToChannel(1L, newMember);

        // Then
        assertNotNull(result);
        verify(channelRepository).save(testChannel);
        assertTrue(testChannel.getMembers().contains(newMember));
    }

    @Test
    void addMemberToChannel_InvalidChannel_ThrowsException() {
        // Given
        User newMember = new User("newuser", "new@example.com", "password");
        when(channelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> channelService.addMemberToChannel(999L, newMember));
        
        assertEquals("Channel not found", exception.getMessage());
    }

    @Test
    void removeMemberFromChannel_ValidChannel_RemovesMember() {
        // Given
        User memberToRemove = new User("removeuser", "remove@example.com", "password");
        memberToRemove.setId(2L);
        testChannel.getMembers().add(memberToRemove);
        
        when(channelRepository.findById(1L)).thenReturn(Optional.of(testChannel));
        when(channelRepository.save(any(Channel.class))).thenReturn(testChannel);

        // When
        Channel result = channelService.removeMemberFromChannel(1L, memberToRemove);

        // Then
        assertNotNull(result);
        verify(channelRepository).save(testChannel);
        assertFalse(testChannel.getMembers().contains(memberToRemove));
    }
}
