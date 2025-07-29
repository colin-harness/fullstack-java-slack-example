package com.example.slackchat.controller;

import com.example.slackchat.dto.ChannelRequest;
import com.example.slackchat.model.Channel;
import com.example.slackchat.model.User;
import com.example.slackchat.repository.ChannelRepository;
import com.example.slackchat.repository.UserRepository;
import com.example.slackchat.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChannelControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        userRepository.deleteAll();
        channelRepository.deleteAll();

        // Create test user
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);
        
        // Generate JWT token
        jwtToken = jwtUtils.generateToken(testUser);
    }

    @Test
    @Transactional
    void createChannel_ValidRequest_ReturnsChannel() throws Exception {
        // Given
        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setName("general");
        channelRequest.setDescription("General discussion");

        // When & Then
        mockMvc.perform(post("/api/channels")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(channelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("General discussion"));
    }

    @Test
    @Transactional
    void createChannel_DuplicateName_ReturnsError() throws Exception {
        // Given
        Channel existingChannel = new Channel("general", "Existing channel", testUser);
        channelRepository.save(existingChannel);

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setName("general");
        channelRequest.setDescription("New general channel");

        // When & Then
        mockMvc.perform(post("/api/channels")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(channelRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Channel name already exists"));
    }

    @Test
    @Transactional
    void getAllChannels_ReturnsPublicChannels() throws Exception {
        // Given
        Channel publicChannel = new Channel("public", "Public channel", testUser);
        channelRepository.save(publicChannel);

        // When & Then
        mockMvc.perform(get("/api/channels")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("public"));
    }

    @Test
    @Transactional
    void getMyChannels_ReturnsUserChannels() throws Exception {
        // Given
        Channel myChannel = new Channel("mychannel", "My channel", testUser);
        channelRepository.save(myChannel);

        // When & Then
        mockMvc.perform(get("/api/channels/my")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("mychannel"));
    }

    @Test
    @Transactional
    void joinChannel_ValidChannel_ReturnsSuccess() throws Exception {
        // Given
        User anotherUser = new User("another", "another@example.com", passwordEncoder.encode("password"));
        anotherUser = userRepository.save(anotherUser);
        
        Channel channel = new Channel("joinable", "Joinable channel", anotherUser);
        channel = channelRepository.save(channel);

        // When & Then
        mockMvc.perform(post("/api/channels/" + channel.getId() + "/join")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("joinable"));
    }

    @Test
    @Transactional
    void getChannelById_ValidId_ReturnsChannel() throws Exception {
        // Given
        Channel channel = new Channel("testchannel", "Test channel", testUser);
        channel = channelRepository.save(channel);

        // When & Then
        mockMvc.perform(get("/api/channels/" + channel.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testchannel"));
    }

    @Test
    @Transactional
    void getChannelById_InvalidId_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/channels/999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createChannel_NoAuth_ReturnsUnauthorized() throws Exception {
        // Given
        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setName("unauthorized");

        // When & Then
        mockMvc.perform(post("/api/channels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(channelRequest)))
                .andExpect(status().isUnauthorized());
    }
}
