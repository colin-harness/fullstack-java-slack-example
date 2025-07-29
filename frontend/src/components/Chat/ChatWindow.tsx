import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  TextField,
  IconButton,
  Typography,
  List,
  ListItem,
  Avatar,
  Chip,
  Alert
} from '@mui/material';
import { Send as SendIcon, Person as PersonIcon } from '@mui/icons-material';
import { Channel, Message, CreateMessageRequest } from '../../types/types';
import { messageService } from '../../services/messageService';
import { useAuth } from '../../contexts/AuthContext';

interface ChatWindowProps {
  channel: Channel;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ channel }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const { user } = useAuth();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadMessages();
  }, [channel.id]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadMessages = async () => {
    try {
      setLoading(true);
      setError('');
      const messagesData = await messageService.getMessagesByChannel(channel.id);
      setMessages(messagesData.reverse()); // Reverse to show oldest first
    } catch (err: any) {
      setError('Failed to load messages');
      console.error('Error loading messages:', err);
    } finally {
      setLoading(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newMessage.trim() || sending) {
      return;
    }

    try {
      setSending(true);
      const messageData: CreateMessageRequest = {
        content: newMessage.trim(),
        channelId: channel.id
      };

      const sentMessage = await messageService.createMessage(messageData);
      setMessages(prev => [...prev, sentMessage]);
      setNewMessage('');
    } catch (err: any) {
      setError('Failed to send message');
      console.error('Error sending message:', err);
    } finally {
      setSending(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e as any);
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Yesterday';
    } else {
      return date.toLocaleDateString();
    }
  };

  const shouldShowDateSeparator = (currentMessage: Message, previousMessage: Message | null) => {
    if (!previousMessage) return true;
    
    const currentDate = new Date(currentMessage.createdAt).toDateString();
    const previousDate = new Date(previousMessage.createdAt).toDateString();
    
    return currentDate !== previousDate;
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
        <Typography>Loading messages...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ height: 'calc(100vh - 64px)', display: 'flex', flexDirection: 'column' }}>
      {/* Channel Header */}
      <Paper elevation={1} sx={{ p: 2, borderRadius: 0 }}>
        <Typography variant="h6">#{channel.name}</Typography>
        {channel.description && (
          <Typography variant="body2" color="text.secondary">
            {channel.description}
          </Typography>
        )}
        <Box sx={{ mt: 1 }}>
          <Chip 
            label={`${channel.members.length} members`} 
            size="small" 
            variant="outlined" 
          />
        </Box>
      </Paper>

      {/* Messages Area */}
      <Box sx={{ flexGrow: 1, overflow: 'auto', p: 1 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <List>
          {messages.map((message, index) => {
            const previousMessage = index > 0 ? messages[index - 1] : null;
            const showDateSeparator = shouldShowDateSeparator(message, previousMessage);
            const isOwnMessage = message.sender.id === user?.id;

            return (
              <React.Fragment key={message.id}>
                {showDateSeparator && (
                  <Box sx={{ textAlign: 'center', my: 2 }}>
                    <Chip 
                      label={formatDate(message.createdAt)} 
                      size="small" 
                      variant="outlined" 
                    />
                  </Box>
                )}
                
                <ListItem
                  sx={{
                    alignItems: 'flex-start',
                    py: 1,
                    backgroundColor: isOwnMessage ? 'action.hover' : 'transparent',
                    borderRadius: 1,
                    mb: 0.5
                  }}
                >
                  <Avatar sx={{ mr: 2, mt: 0.5, width: 32, height: 32 }}>
                    <PersonIcon />
                  </Avatar>
                  <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                    <Box sx={{ display: 'flex', alignItems: 'baseline', mb: 0.5 }}>
                      <Typography variant="subtitle2" sx={{ mr: 1, fontWeight: 'bold' }}>
                        {message.sender.username}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatTime(message.createdAt)}
                      </Typography>
                    </Box>
                    <Typography variant="body1" sx={{ wordBreak: 'break-word' }}>
                      {message.content}
                    </Typography>
                  </Box>
                </ListItem>
              </React.Fragment>
            );
          })}
        </List>
        <div ref={messagesEndRef} />
      </Box>

      {/* Message Input */}
      <Paper elevation={2} sx={{ p: 2, borderRadius: 0 }}>
        <Box component="form" onSubmit={handleSendMessage} sx={{ display: 'flex', gap: 1 }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder={`Message #${channel.name}`}
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={sending}
            size="small"
            multiline
            maxRows={4}
          />
          <IconButton 
            type="submit" 
            color="primary" 
            disabled={!newMessage.trim() || sending}
            sx={{ alignSelf: 'flex-end' }}
          >
            <SendIcon />
          </IconButton>
        </Box>
      </Paper>
    </Box>
  );
};

export default ChatWindow;
