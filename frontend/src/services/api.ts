import type { AuthResponse, GuestAuthResponse, OnlineStats, User } from '../types/auth';
import type { ConversationDetail, ConversationSummary } from '../types/chat';

const rawBaseUrl = (import.meta.env.VITE_API_URL as string | undefined) || '';
export const API_BASE_URL = rawBaseUrl.replace(/\/+$/, '');
const API_BASE = API_BASE_URL ? `${API_BASE_URL}/api` : '/api';

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('stranger_token');
  if (token) {
    return { Authorization: `Bearer ${token}` };
  }
  return {};
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = {
    'Content-Type': 'application/json',
    ...getAuthHeader(),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMsg = 'An unexpected error occurred';
    try {
      const data = await response.json();
      errorMsg = data.message || errorMsg;
    } catch {
      errorMsg = response.statusText || errorMsg;
    }
    throw new Error(errorMsg);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const api = {
  register: (body: { username: string; email: string; password: string }) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(body) }),

  login: (body: { usernameOrEmail: string; password: string }) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify(body) }),

  guest: () =>
    request<GuestAuthResponse>('/auth/guest', { method: 'POST' }),

  getMe: () =>
    request<User>('/users/me'),

  getOnlineStats: () =>
    request<OnlineStats>('/stats/online'),

  getConversations: (page = 0, size = 15) =>
    request<{ content: ConversationSummary[]; totalPages: number; totalElements: number }>(
      `/conversations?page=${page}&size=${size}`
    ),

  getConversationDetail: (id: string) =>
    request<ConversationDetail>(`/conversations/${id}`),

  deleteConversation: (id: string) =>
    request<void>(`/conversations/${id}`, { method: 'DELETE' }),

  submitReport: (body: { conversationId?: string; reason: string; details?: string }) =>
    request<{ message: string }>('/reports', { method: 'POST', body: JSON.stringify(body) }),

  blockUser: (body: { roomId?: string; conversationId?: string }) =>
    request<{ message: string }>('/blocks', { method: 'POST', body: JSON.stringify(body) }),
};
