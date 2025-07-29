package com.example.slackchat.repository;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m = :user")
    List<Channel> findChannelsByMember(@Param("user") User user);
    
    @Query("SELECT c FROM Channel c WHERE c.isPrivate = false")
    List<Channel> findPublicChannels();
}
