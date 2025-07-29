import React, { useState, useEffect } from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Typography,
  Divider,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Chip,
  IconButton,
  Toolbar
} from '@mui/material';
import { Add as AddIcon, Tag as TagIcon, Lock as LockIcon } from '@mui/icons-material';
import { Channel, CreateChannelRequest } from '../../types/types';
import { channelService } from '../../services/channelService';

interface ChannelListProps {
  selectedChannel: Channel | null;
  onChannelSelect: (channel: Channel) => void;
}

const ChannelList: React.FC<ChannelListProps> = ({ selectedChannel, onChannelSelect }) => {
  const [myChannels, setMyChannels] = useState<Channel[]>([]);
  const [publicChannels, setPublicChannels] = useState<Channel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [newChannelName, setNewChannelName] = useState('');
  const [newChannelDescription, setNewChannelDescription] = useState('');
  const [createError, setCreateError] = useState('');

  useEffect(() => {
    loadChannels();
  }, []);

  const loadChannels = async () => {
    try {
      setLoading(true);
      const [myChannelsData, publicChannelsData] = await Promise.all([
        channelService.getMyChannels(),
        channelService.getAllChannels()
      ]);
      setMyChannels(myChannelsData);
      setPublicChannels(publicChannelsData);
    } catch (err: any) {
      setError('Failed to load channels');
      console.error('Error loading channels:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateChannel = async () => {
    if (!newChannelName.trim()) {
      setCreateError('Channel name is required');
      return;
    }

    try {
      const channelData: CreateChannelRequest = {
        name: newChannelName.trim(),
        description: newChannelDescription.trim() || undefined,
        isPrivate: false
      };

      const newChannel = await channelService.createChannel(channelData);
      setMyChannels(prev => [...prev, newChannel]);
      setCreateDialogOpen(false);
      setNewChannelName('');
      setNewChannelDescription('');
      setCreateError('');
      onChannelSelect(newChannel);
    } catch (err: any) {
      setCreateError(err.response?.data?.message || 'Failed to create channel');
    }
  };

  const handleJoinChannel = async (channel: Channel) => {
    try {
      await channelService.joinChannel(channel.id);
      setMyChannels(prev => [...prev, channel]);
      onChannelSelect(channel);
    } catch (err: any) {
      setError('Failed to join channel');
    }
  };

  const isChannelJoined = (channel: Channel) => {
    return myChannels.some(myChannel => myChannel.id === channel.id);
  };

  if (loading) {
    return (
      <Box sx={{ p: 2 }}>
        <Typography>Loading channels...</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          Channels
        </Typography>
        <IconButton onClick={() => setCreateDialogOpen(true)} size="small">
          <AddIcon />
        </IconButton>
      </Toolbar>
      
      <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
        {error && (
          <Alert severity="error" sx={{ m: 1 }}>
            {error}
          </Alert>
        )}

        {/* My Channels */}
        <Typography variant="subtitle2" sx={{ px: 2, py: 1, fontWeight: 'bold' }}>
          My Channels
        </Typography>
        <List dense>
          {myChannels.map((channel) => (
            <ListItem key={channel.id} disablePadding>
              <ListItemButton
                selected={selectedChannel?.id === channel.id}
                onClick={() => onChannelSelect(channel)}
              >
                <TagIcon sx={{ mr: 1, fontSize: 16 }} />
                <ListItemText 
                  primary={channel.name}
                  secondary={channel.description}
                />
                {channel.isPrivate && <LockIcon sx={{ fontSize: 16, ml: 1 }} />}
              </ListItemButton>
            </ListItem>
          ))}
        </List>

        <Divider sx={{ my: 1 }} />

        {/* Public Channels */}
        <Typography variant="subtitle2" sx={{ px: 2, py: 1, fontWeight: 'bold' }}>
          Browse Channels
        </Typography>
        <List dense>
          {publicChannels
            .filter(channel => !isChannelJoined(channel))
            .map((channel) => (
            <ListItem key={channel.id} disablePadding>
              <ListItemButton onClick={() => handleJoinChannel(channel)}>
                <TagIcon sx={{ mr: 1, fontSize: 16 }} />
                <ListItemText 
                  primary={channel.name}
                  secondary={channel.description}
                />
                <Chip label="Join" size="small" variant="outlined" />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Box>

      {/* Create Channel Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Channel</DialogTitle>
        <DialogContent>
          {createError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {createError}
            </Alert>
          )}
          <TextField
            autoFocus
            margin="dense"
            label="Channel Name"
            fullWidth
            variant="outlined"
            value={newChannelName}
            onChange={(e) => setNewChannelName(e.target.value)}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Description (optional)"
            fullWidth
            multiline
            rows={3}
            variant="outlined"
            value={newChannelDescription}
            onChange={(e) => setNewChannelDescription(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateChannel} variant="contained">Create</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ChannelList;
