import axiosClient from './axiosClient'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
  Account,
  AccountSearchParams,
  CreateAccountRequest,
  UpdateAccountRequest,
  Status,
} from '@/types/account'

export const accountApi = {
  // Lấy danh sách tài khoản (có tìm kiếm, lọc, phân trang)
  getAccounts: (params?: AccountSearchParams) => {
    return axiosClient.get<unknown, ApiResponse<PageResponse<Account>>>('/accounts', { params })
  },

  exportAccounts: (params?: AccountSearchParams) => {
    return axiosClient.get<unknown, Blob>('/accounts/export', { params, responseType: 'blob' })
  },

  // Lấy chi tiết tài khoản theo ID
  getAccountById: (id: number) => {
    return axiosClient.get<unknown, ApiResponse<Account>>(`/accounts/${id}`)
  },

  // Tạo tài khoản mới
  createAccount: (data: CreateAccountRequest) => {
    return axiosClient.post<unknown, ApiResponse<Account>>('/accounts', data)
  },

  // Cập nhật thông tin tài khoản
  updateAccount: (id: number, data: UpdateAccountRequest) => {
    return axiosClient.put<unknown, ApiResponse<Account>>(`/accounts/${id}`, data)
  },

  // Đổi trạng thái 1 tài khoản (ACTIVE, PENDING, BLOCKED, DELETED)
  updateStatus: (id: number, status: Status) => {
    return axiosClient.patch<unknown, ApiResponse<Account>>(`/accounts/${id}/status`, { status })
  },

  // Đổi trạng thái hàng loạt tài khoản
  updateStatusList: (ids: number[], status: Status) => {
    return axiosClient.patch<unknown, ApiResponse<Account[]>>('/accounts/updateStatusList', { ids, status })
  },
}

