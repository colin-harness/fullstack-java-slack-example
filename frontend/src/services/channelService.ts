import axios from 'axios';
import { Channel, CreateChannelRequest } from '../types/types';
import { authService } from './authService';

const API_URL = '/api/channels';

class ChannelService {
  async getAllChannels(): Promise<Channel[]> {
    const response = await axios.get(API_URL, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async getMyChannels(): Promise<Channel[]> {
    const response = await axios.get(`${API_URL}/my`, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async getChannelById(id: number): Promise<Channel> {
    const response = await axios.get(`${API_URL}/${id}`, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async createChannel(channelData: CreateChannelRequest): Promise<Channel> {
    const response = await axios.post(API_URL, channelData, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async joinChannel(channelId: number): Promise<Channel> {
    const response = await axios.post(`${API_URL}/${channelId}/join`, {}, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async leaveChannel(channelId: number): Promise<Channel> {
    const response = await axios.post(`${API_URL}/${channelId}/leave`, {}, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }
}

export const channelService = new ChannelService();
