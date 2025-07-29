package com.example.slackchat.service;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.User;
import com.example.slackchat.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    public Channel createChannel(String name, String description, User createdBy) {
        if (channelRepository.existsByName(name)) {
            throw new RuntimeException("Channel name already exists");
        }

        Channel channel = new Channel(name, description, createdBy);
        return channelRepository.save(channel);
    }

    public Optional<Channel> findById(Long id) {
        return channelRepository.findById(id);
    }

    public Optional<Channel> findByName(String name) {
        return channelRepository.findByName(name);
    }

    public List<Channel> findAllChannels() {
        return channelRepository.findAll();
    }

    public List<Channel> findPublicChannels() {
        return channelRepository.findPublicChannels();
    }

    public List<Channel> findChannelsByMember(User user) {
        return channelRepository.findChannelsByMember(user);
    }

    public Channel addMemberToChannel(Long channelId, User user) {
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            channel.getMembers().add(user);
            return channelRepository.save(channel);
        }
        throw new RuntimeException("Channel not found");
    }

    public Channel removeMemberFromChannel(Long channelId, User user) {
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            channel.getMembers().remove(user);
            return channelRepository.save(channel);
        }
        throw new RuntimeException("Channel not found");
    }

    public void deleteChannel(Long channelId) {
        channelRepository.deleteById(channelId);
    }
}
