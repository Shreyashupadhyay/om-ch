export interface ChatMessage {
  id: string;
  senderLabel: 'You' | 'Stranger' | 'System';
  fromMe: boolean;
  content: string;
  timestamp: string;
  isSystem?: boolean;
}

export type QueueState = 'IDLE' | 'WAITING' | 'CONNECTED' | 'PARTNER_LEFT' | 'DISCONNECTED';

export interface WsEventMessage {
  event: 'WAITING_IN_QUEUE' | 'MATCHED' | 'PARTNER_TYPING' | 'PARTNER_STOPPED_TYPING' | 'PARTNER_LEFT' | 'ROOM_ENDED' | 'SYSTEM_NOTICE' | 'ERROR';
  roomId?: string;
  message?: string;
  payload?: Record<string, any>;
  timestamp?: string;
}

export interface ConversationSummary {
  id: string;
  startedAt: string;
  endedAt?: string;
  durationSeconds: number;
  messageCount: number;
  lastMessagePreview: string;
  partnerLabel: string;
}

export interface ConversationDetail {
  id: string;
  startedAt: string;
  endedAt?: string;
  durationSeconds: number;
  endedBy?: string;
  partnerLabel: string;
  messages: {
    id: string;
    senderLabel: string;
    fromMe: boolean;
    content: string;
    sentAt: string;
  }[];
}
