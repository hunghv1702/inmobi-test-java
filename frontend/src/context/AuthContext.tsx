import React, { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../api/authApi';
import { APP_CONFIG } from '../config/env.config';
import type { CurrentUser } from '../types/api';

interface AuthContextType {
  user: CurrentUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  register: (email: string, pass: string) => Promise<void>;
  logout: () => void;
  updateUser: (updatedUser: CurrentUser) => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN)
  );
  const [isLoading, setIsLoading] = useState(true);

  const fetchUserProfile = async () => {
    try {
      const profile = await authApi.getMe();
      setUser(profile);
    } catch {
      logout();
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (token) {
      fetchUserProfile();
    } else {
      setIsLoading(false);
    }

    const handleUnauthorized = () => {
      logout();
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, [token]);

  const login = async (email: string, pass: string) => {
    const authData = await authApi.login(email, pass);
    localStorage.setItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN, authData.accessToken);
    setToken(authData.accessToken);
    const profile = await authApi.getMe();
    setUser(profile);
  };

  const register = async (email: string, pass: string) => {
    await authApi.register(email, pass);
  };

  const logout = () => {
    localStorage.removeItem(APP_CONFIG.STORAGE_KEYS.AUTH_TOKEN);
    setToken(null);
    setUser(null);
  };

  const updateUser = (updatedUser: CurrentUser) => {
    setUser(updatedUser);
  };

  const refreshProfile = async () => {
    if (token) {
      const profile = await authApi.getMe();
      setUser(profile);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        updateUser,
        refreshProfile,
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
