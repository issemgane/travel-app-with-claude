import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { api } from './api';
import type { User } from '@/types';

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (username: string, displayName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  token: null,
  login: async () => {},
  register: async () => {},
  logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Restore session from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('wanderlust_token');
    if (saved) {
      setToken(saved);
      api.setToken(saved);
      api.getMe()
        .then(setUser)
        .catch(() => {
          localStorage.removeItem('wanderlust_token');
          setToken(null);
          api.setToken(null);
        })
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.login(email, password);
    localStorage.setItem('wanderlust_token', res.token);
    setToken(res.token);
    api.setToken(res.token);
    const me = await api.getMe();
    setUser(me);
  }, []);

  const register = useCallback(async (username: string, displayName: string, email: string, password: string) => {
    const res = await api.register(username, displayName, email, password);
    localStorage.setItem('wanderlust_token', res.token);
    setToken(res.token);
    api.setToken(res.token);
    const me = await api.getMe();
    setUser(me);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('wanderlust_token');
    setToken(null);
    setUser(null);
    api.setToken(null);
  }, []);

  return (
    <AuthContext.Provider value={{ isAuthenticated: !!user, isLoading, user, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
