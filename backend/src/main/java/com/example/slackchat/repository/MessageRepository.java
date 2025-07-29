package com.example.slackchat.repository;

import com.example.slackchat.model.Channel;
import com.example.slackchat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.channel = :channel ORDER BY m.createdAt DESC")
    Page<Message> findByChannelOrderByCreatedAtDesc(@Param("channel") Channel channel, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.channel = :channel ORDER BY m.createdAt ASC")
    List<Message> findByChannelOrderByCreatedAtAsc(@Param("channel") Channel channel);
    
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC")
    List<Message> findRecentMessagesByChannelId(@Param("channelId") Long channelId, Pageable pageable);
}
