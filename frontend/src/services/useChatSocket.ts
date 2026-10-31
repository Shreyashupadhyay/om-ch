import { useCallback, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ChatMessage, QueueState, WsEventMessage } from '../types/chat';

export function useChatSocket(token: string | null) {
  const [queueState, setQueueState] = useState<QueueState>('IDLE');
  const [roomId, setRoomId] = useState<string | null>(null);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isPartnerTyping, setIsPartnerTyping] = useState<boolean>(false);
  const [statusMessage, setStatusMessage] = useState<string>('Ready to chat');
  const [isConnectedToServer, setIsConnectedToServer] = useState<boolean>(false);

  const clientRef = useRef<Client | null>(null);
  const typingTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!token) {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      setIsConnectedToServer(false);
      return;
    }

    const rawBaseUrl = (import.meta.env.VITE_API_URL as string | undefined) || '';
    const backendBase = rawBaseUrl.replace(/\/+$/, '');
    const wsUrl = backendBase ? `${backendBase}/ws` : '/ws';

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: () => {
        // Suppress verbose debug logs in production
      },
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setIsConnectedToServer(true);

      client.subscribe('/user/queue/messages', (msg: IMessage) => {
        try {
          const payload = JSON.parse(msg.body);
          setMessages((prev) => [
            ...prev,
            {
              id: payload.id || Math.random().toString(),
              senderLabel: payload.senderLabel || (payload.fromMe ? 'You' : 'Stranger'),
              fromMe: !!payload.fromMe,
              content: payload.content,
              timestamp: payload.timestamp || new Date().toISOString(),
            },
          ]);
        } catch (err) {
          console.error('Error parsing chat message', err);
        }
      });

      client.subscribe('/user/queue/events', (msg: IMessage) => {
        try {
          const eventMsg: WsEventMessage = JSON.parse(msg.body);
          handleEventMessage(eventMsg);
        } catch (err) {
          console.error('Error parsing event message', err);
        }
      });
    };

    client.onDisconnect = () => {
      setIsConnectedToServer(false);
    };

    client.onStompError = (frame) => {
      console.error('STOMP broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
      setStatusMessage('Connection error. Reconnecting...');
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setIsConnectedToServer(false);
    };
  }, [token]);

  const handleEventMessage = (eventMsg: WsEventMessage) => {
    switch (eventMsg.event) {
      case 'WAITING_IN_QUEUE':
        setQueueState('WAITING');
        setRoomId(null);
        setConversationId(null);
        setIsPartnerTyping(false);
        setStatusMessage(eventMsg.message || 'Looking for someone to chat with...');
        break;

      case 'MATCHED':
        setQueueState('CONNECTED');
        setRoomId(eventMsg.roomId || null);
        setConversationId(eventMsg.payload?.conversationId || null);
        setIsPartnerTyping(false);
        setStatusMessage('Connected with a stranger. Say hi!');
        setMessages([
          {
            id: 'sys-start-' + Date.now(),
            senderLabel: 'System',
            fromMe: false,
            content: 'You are now chatting with a random stranger. Say hi!',
            timestamp: new Date().toISOString(),
            isSystem: true,
          },
        ]);
        break;

      case 'PARTNER_TYPING':
        setIsPartnerTyping(true);
        if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = setTimeout(() => {
          setIsPartnerTyping(false);
        }, 3000);
        break;

      case 'PARTNER_STOPPED_TYPING':
        setIsPartnerTyping(false);
        break;

      case 'PARTNER_LEFT':
        setQueueState('PARTNER_LEFT');
        setIsPartnerTyping(false);
        setStatusMessage('Stranger has disconnected.');
        setMessages((prev) => [
          ...prev,
          {
            id: 'sys-left-' + Date.now(),
            senderLabel: 'System',
            fromMe: false,
            content: 'Stranger has disconnected.',
            timestamp: new Date().toISOString(),
            isSystem: true,
          },
        ]);
        break;

      case 'ROOM_ENDED':
        setQueueState('PARTNER_LEFT');
        setIsPartnerTyping(false);
        setStatusMessage('You disconnected.');
        setMessages((prev) => [
          ...prev,
          {
            id: 'sys-ended-' + Date.now(),
            senderLabel: 'System',
            fromMe: false,
            content: 'You have disconnected.',
            timestamp: new Date().toISOString(),
            isSystem: true,
          },
        ]);
        break;

      case 'ERROR':
        setStatusMessage(eventMsg.message || 'An error occurred.');
        setMessages((prev) => [
          ...prev,
          {
            id: 'sys-err-' + Date.now(),
            senderLabel: 'System',
            fromMe: false,
            content: eventMsg.message || 'Error occurred',
            timestamp: new Date().toISOString(),
            isSystem: true,
          },
        ]);
        break;

      default:
        break;
    }
  };

  const joinQueue = useCallback(() => {
    if (clientRef.current && clientRef.current.connected) {
      setMessages([]);
      setQueueState('WAITING');
      setStatusMessage('Looking for someone you can chat with...');
      clientRef.current.publish({ destination: '/app/queue.join' });
    }
  }, []);

  const leaveQueue = useCallback(() => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({ destination: '/app/queue.leave' });
      setQueueState('IDLE');
      setStatusMessage('You left the queue.');
    }
  }, []);

  const sendMessage = useCallback(
    (content: string) => {
      if (!roomId || !content.trim()) return;
      if (clientRef.current && clientRef.current.connected) {
        clientRef.current.publish({
          destination: '/app/chat.send',
          body: JSON.stringify({
            roomId,
            content: content.trim(),
          }),
        });
      }
    },
    [roomId]
  );

  const sendTyping = useCallback(
    (isTyping: boolean) => {
      if (!roomId) return;
      if (clientRef.current && clientRef.current.connected) {
        clientRef.current.publish({
          destination: '/app/chat.typing',
          body: JSON.stringify({
            roomId,
            isTyping,
          }),
        });
      }
    },
    [roomId]
  );

  const nextStranger = useCallback(() => {
    if (clientRef.current && clientRef.current.connected) {
      setMessages([]);
      setQueueState('WAITING');
      setStatusMessage('Finding a new stranger...');
      clientRef.current.publish({ destination: '/app/chat.next' });
    }
  }, []);

  const leaveRoom = useCallback(() => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({ destination: '/app/chat.leave' });
      setQueueState('PARTNER_LEFT');
    }
  }, []);

  return {
    queueState,
    roomId,
    conversationId,
    messages,
    isPartnerTyping,
    statusMessage,
    isConnectedToServer,
    joinQueue,
    leaveQueue,
    sendMessage,
    sendTyping,
    nextStranger,
    leaveRoom,
  };
}
