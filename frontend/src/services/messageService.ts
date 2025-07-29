import axios from 'axios';
import { Message, CreateMessageRequest } from '../types/types';
import { authService } from './authService';

const API_URL = '/api/messages';

class MessageService {
  async getMessagesByChannel(channelId: number, limit: number = 50): Promise<Message[]> {
    const response = await axios.get(`${API_URL}/channel/${channelId}`, {
      params: { limit },
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async getMessagesByChannelPaginated(
    channelId: number, 
    page: number = 0, 
    size: number = 20
  ): Promise<{ content: Message[], totalElements: number, totalPages: number }> {
    const response = await axios.get(`${API_URL}/channel/${channelId}/paginated`, {
      params: { page, size },
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async createMessage(messageData: CreateMessageRequest): Promise<Message> {
    const response = await axios.post(API_URL, messageData, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async updateMessage(messageId: number, content: string): Promise<Message> {
    const response = await axios.put(`${API_URL}/${messageId}`, 
      { content, channelId: 0 }, // channelId not used for updates
      {
        headers: authService.getAuthHeader()
      }
    );
    return response.data;
  }

  async deleteMessage(messageId: number): Promise<void> {
    await axios.delete(`${API_URL}/${messageId}`, {
      headers: authService.getAuthHeader()
    });
  }
}

export const messageService = new MessageService();
