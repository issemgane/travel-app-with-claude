import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import Keycloak from 'keycloak-js';
import { api } from './api';
import type { User } from '@/types';

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'wanderlust',
  clientId: 'wanderlust-frontend',
});

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  login: () => void;
  logout: () => void;
  token: string | null;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  login: () => {},
  logout: () => {},
  token: null,
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    keycloak
      .init({ onLoad: 'check-sso', silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html' })
      .then(async (authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated && keycloak.token) {
          setToken(keycloak.token);
          api.setToken(keycloak.token);
          try {
            const me = await api.getMe();
            setUser(me);
          } catch {
            // User fetch failed, continue unauthenticated
          }
        }
        setIsLoading(false);
      })
      .catch(() => setIsLoading(false));

    // Token refresh
    const interval = setInterval(() => {
      if (keycloak.authenticated) {
        keycloak.updateToken(30).then((refreshed) => {
          if (refreshed && keycloak.token) {
            setToken(keycloak.token);
            api.setToken(keycloak.token);
          }
        });
      }
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  const login = useCallback(() => keycloak.login(), []);
  const logout = useCallback(() => {
    keycloak.logout();
    setUser(null);
    setToken(null);
    api.setToken(null);
  }, []);

  return (
    <AuthContext.Provider value={{ isAuthenticated, isLoading, user, login, logout, token }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
