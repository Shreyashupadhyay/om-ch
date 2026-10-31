import React, { createContext, useContext, useEffect, useState } from 'react';
import type { User } from '../types/auth';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isGuest: boolean;
  displayName: string;
  isAuthenticated: boolean;
  isLoading: boolean;
  hasAgreedToTerms: boolean;
  login: (usernameOrEmail: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  loginAsGuest: () => Promise<string>;
  logout: () => void;
  setAgreedToTerms: (agreed: boolean) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('stranger_token'));
  const [isGuest, setIsGuest] = useState<boolean>(() => localStorage.getItem('stranger_is_guest') === 'true');
  const [displayName, setDisplayName] = useState<string>(() => localStorage.getItem('stranger_display_name') || '');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [hasAgreedToTerms, setHasAgreedToTermsState] = useState<boolean>(
    () => localStorage.getItem('stranger_terms_agreed') === 'true'
  );

  const setAgreedToTerms = (agreed: boolean) => {
    setHasAgreedToTermsState(agreed);
    localStorage.setItem('stranger_terms_agreed', agreed ? 'true' : 'false');
  };

  useEffect(() => {
    async function loadCurrentUser() {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        const profile = await api.getMe();
        setUser(profile);
        setIsGuest(profile.role === 'ROLE_GUEST');
        setDisplayName(profile.username);
        localStorage.setItem('stranger_display_name', profile.username);
        localStorage.setItem('stranger_is_guest', profile.role === 'ROLE_GUEST' ? 'true' : 'false');
      } catch {
        // Invalid or expired token
        localStorage.removeItem('stranger_token');
        localStorage.removeItem('stranger_is_guest');
        localStorage.removeItem('stranger_display_name');
        setToken(null);
        setUser(null);
        setIsGuest(false);
        setDisplayName('');
      } finally {
        setIsLoading(false);
      }
    }

    loadCurrentUser();
  }, [token]);

  const login = async (usernameOrEmail: string, password: string) => {
    const res = await api.login({ usernameOrEmail, password });
    localStorage.setItem('stranger_token', res.token);
    localStorage.setItem('stranger_is_guest', 'false');
    localStorage.setItem('stranger_display_name', res.user.username);
    setToken(res.token);
    setUser(res.user);
    setIsGuest(false);
    setDisplayName(res.user.username);
  };

  const register = async (username: string, email: string, password: string) => {
    const res = await api.register({ username, email, password });
    localStorage.setItem('stranger_token', res.token);
    localStorage.setItem('stranger_is_guest', 'false');
    localStorage.setItem('stranger_display_name', res.user.username);
    setToken(res.token);
    setUser(res.user);
    setIsGuest(false);
    setDisplayName(res.user.username);
  };

  const loginAsGuest = async (): Promise<string> => {
    const res = await api.guest();
    localStorage.setItem('stranger_token', res.token);
    localStorage.setItem('stranger_is_guest', 'true');
    localStorage.setItem('stranger_display_name', res.displayName);
    setToken(res.token);
    setIsGuest(true);
    setDisplayName(res.displayName);
    setUser({
      username: res.displayName,
      role: 'ROLE_GUEST',
    });
    return res.token;
  };

  const logout = () => {
    localStorage.removeItem('stranger_token');
    localStorage.removeItem('stranger_is_guest');
    localStorage.removeItem('stranger_display_name');
    setToken(null);
    setUser(null);
    setIsGuest(false);
    setDisplayName('');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isGuest,
        displayName,
        isAuthenticated: !!token,
        isLoading,
        hasAgreedToTerms,
        login,
        register,
        loginAsGuest,
        logout,
        setAgreedToTerms,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
