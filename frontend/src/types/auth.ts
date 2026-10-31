export interface User {
  id?: string;
  username: string;
  email?: string;
  role: string;
  createdAt?: string;
  lastLoginAt?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface GuestAuthResponse {
  token: string;
  tokenType: string;
  guestId: string;
  displayName: string;
  isGuest: boolean;
}

export interface OnlineStats {
  onlineUsers: number;
  activeRooms: number;
  queueLength: number;
}
