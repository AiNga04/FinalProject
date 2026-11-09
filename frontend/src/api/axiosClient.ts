import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'
import type { LoginResponse } from '@/types/auth'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

// Instance axios không có interceptor, dùng riêng để gọi /auth/refresh tránh vòng lặp vô tận
const axiosRefresh = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
  timeout: 15000,
})

/**
 * 🔒 BẢO MẬT CHUẨN XSS & CSRF:
 * - Access Token: Lưu hoàn toàn trong RUNTIME MEMORY (biến in-memory), KHÔNG lưu trong localStorage.
 * - Refresh Token: Do Server lưu và quản lý trong HTTPONLY COOKIE, JavaScript không thể đọc được.
 */
let inMemoryAccessToken: string | null = null

// Getter & Setter cho Access Token trong bộ nhớ Runtime
export const getAccessToken = (): string | null => inMemoryAccessToken
export const setAccessToken = (token: string | null): void => {
  inMemoryAccessToken = token
}

// Khởi tạo instance Axios với withCredentials = true để tự động gửi HttpOnly Cookie
const axiosClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Bắt buộc để trình duyệt tự động gửi và nhận HttpOnly Cookie
  timeout: 15000,
})

// Quản lý hàng đợi khi refresh token đang chạy
let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else if (token) {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// 1. Request Interceptor: Lấy Access Token từ Runtime Memory gắn vào Header
axiosClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 2. Response Interceptor: Trả về response.data & Tự động gọi /auth/refresh qua HttpOnly Cookie khi 401
axiosClient.interceptors.response.use(
  (response) => {
    return response.data
  },
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (!error.response) {
      return Promise.reject(error)
    }

    const { status } = error.response
    const isAuthEndpoint =
      originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/refresh')

    // Xử lý 401 Unauthorized (Access token hết hạn)
    if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        // Đã có 1 request đang refresh, đưa request này vào hàng đợi chờ token mới
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((newToken) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`
            }
            return axiosClient(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Gọi API /auth/refresh qua axiosRefresh (instance riêng, không có interceptor retry)
        // để tránh vòng lặp vô tận. Trình duyệt tự gửi kèm HttpOnly Cookie!
        const refreshResponse = await axiosRefresh.post<ApiResponse<LoginResponse>>('/auth/refresh')

        const newAccessToken = refreshResponse.data.data.accessToken
        setAccessToken(newAccessToken)

        // Báo cho toàn bộ request đang chờ tiếp tục
        processQueue(null, newAccessToken)

        // Gửi lại request ban đầu với Access Token mới
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        }
        return axiosClient(originalRequest)
      } catch (refreshError) {
        // Refresh Token trong cookie cũng đã hết hạn hoặc bị thu hồi
        processQueue(refreshError, null)
        setAccessToken(null)
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default axiosClient
