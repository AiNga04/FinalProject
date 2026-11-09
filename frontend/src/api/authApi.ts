import axiosClient from './axiosClient'
import type { ApiResponse } from '@/types/api'
import type { LoginRequest, LoginResponse } from '@/types/auth'
import type { Account } from '@/types/account'

export const authApi = {
  // Đăng nhập (trả về accessToken, backend tự động gắn refreshToken vào HttpOnly Cookie)
  login: (data: LoginRequest) => {
    return axiosClient.post<unknown, ApiResponse<LoginResponse>>('/auth/login', data)
  },

  // Làm mới token (browser tự gửi kèm HttpOnly Cookie, không cần truyền refreshToken qua body)
  refreshToken: () => {
    return axiosClient.post<unknown, ApiResponse<LoginResponse>>('/auth/refresh')
  },

  // Đăng xuất (xóa session và xóa HttpOnly Cookie phía server)
  logout: () => {
    return axiosClient.post<unknown, ApiResponse<void>>('/auth/logout')
  },

  // Lấy thông tin tài khoản đang đăng nhập
  getMe: () => {
    return axiosClient.get<unknown, ApiResponse<Account>>('/accounts/me')
  },
}
