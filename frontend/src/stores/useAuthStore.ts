import { create } from "zustand";
import { authApi } from "@/api/authApi";
import { setAccessToken } from "@/api/axiosClient";
import type { Account } from "@/types/account";
import type { LoginRequest } from "@/types/auth";

interface AuthState {
  user: Account | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
  setUser: (user: Account | null) => void;
}

let checkAuthPromise: Promise<void> | null = null;

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  // 1. Đăng nhập
  login: async (credentials: LoginRequest) => {
    try {
      // Gọi API đăng nhập (Backend trả về accessToken trong body và set refreshToken vào HttpOnly Cookie)
      const loginRes = await authApi.login(credentials);
      const { accessToken } = loginRes.data;

      // Lưu Access Token vào Runtime Memory
      setAccessToken(accessToken);

      // Lấy thông tin tài khoản đang đăng nhập
      const userRes = await authApi.getMe();
      set({
        user: userRes.data,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      setAccessToken(null);
      set({ user: null, isAuthenticated: false, isLoading: false });
      throw error;
    }
  },

  // 2. Đăng xuất
  logout: async () => {
    try {
      await authApi.logout();
    } catch {
      // Dù API lỗi vẫn xóa sạch state phía client
    } finally {
      setAccessToken(null);
      set({ user: null, isAuthenticated: false, isLoading: false });
      window.location.href = "/login";
    }
  },

  // 3. Khôi phục phiên đăng nhập khi F5 hoặc mở tab mới
  checkAuth: async () => {
    if (checkAuthPromise) {
      return checkAuthPromise;
    }

    checkAuthPromise = (async () => {
      set({ isLoading: true });
      try {
        // Gọi refresh token ngầm (trình duyệt tự đính kèm HttpOnly Cookie)
        const refreshRes = await authApi.refreshToken();
        const { accessToken } = refreshRes.data;

        setAccessToken(accessToken);

        // Lấy thông tin user
        const userRes = await authApi.getMe();
        set({
          user: userRes.data,
          isAuthenticated: true,
          isLoading: false,
        });
      } catch {
        // Không có cookie hợp lệ hoặc token đã hết hạn
        setAccessToken(null);
        set({
          user: null,
          isAuthenticated: false,
          isLoading: false,
        });
      }
    })();

    try {
      await checkAuthPromise;
    } finally {
      checkAuthPromise = null;
    }
  },

  setUser: (user: Account | null) => set({ user }),
}));
