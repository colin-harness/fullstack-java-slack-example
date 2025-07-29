export interface User {
  id: number;
  username: string;
  email: string;
  displayName?: string;
  bio?: string;
  isOnline?: boolean;
  lastActive?: string;
}

export interface Channel {
  id: number;
  name: string;
  description?: string;
  isPrivate: boolean;
  createdAt: string;
  createdBy: User;
  members: User[];
}

export interface Message {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
  sender: User;
  channel: Channel;
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
}

export interface CreateChannelRequest {
  name: string;
  description?: string;
  isPrivate?: boolean;
}

export interface CreateMessageRequest {
  content: string;
  channelId: number;
}
