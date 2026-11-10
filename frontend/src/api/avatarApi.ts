import axiosClient from './axiosClient'
import type { ApiResponse } from '@/types/api'
import type { AvatarResponse } from '@/types/avatar'

export const avatarApi = {
  getCurrent: () => axiosClient.get<unknown, ApiResponse<AvatarResponse | null>>('/accounts/me/avatar'),
  getByAccount: (accountId: number) => axiosClient.get<unknown, ApiResponse<AvatarResponse | null>>(`/accounts/${accountId}/avatar`),
  getHistoryByAccount: (accountId: number) => axiosClient.get<unknown, ApiResponse<AvatarResponse[]>>(`/accounts/${accountId}/avatars/history`),
  getMyHistory: () => axiosClient.get<unknown, ApiResponse<AvatarResponse[]>>('/accounts/me/avatar/history'),
  getFile: (avatarId: number) => axiosClient.get<unknown, Blob>(`/accounts/avatars/${avatarId}/file`, { responseType: 'blob' }),
  getCurrentFile: () => axiosClient.get<unknown, Blob>('/accounts/me/avatar/file', { responseType: 'blob' }),
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return axiosClient.post<unknown, ApiResponse<AvatarResponse>>('/accounts/me/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  remove: () => axiosClient.delete<unknown, ApiResponse<void>>('/accounts/me/avatar'),
  removeMine: (avatarId: number) => axiosClient.delete<unknown, ApiResponse<void>>(`/accounts/me/avatar/${avatarId}`),
  removeByAccount: (accountId: number, avatarId: number) => axiosClient.delete<unknown, ApiResponse<void>>(`/accounts/${accountId}/avatars/${avatarId}`),
  restore: (accountId: number, avatarId: number) => axiosClient.patch<unknown, ApiResponse<AvatarResponse>>(`/accounts/${accountId}/avatars/${avatarId}/restore`),
  restoreMine: (avatarId: number) => axiosClient.patch<unknown, ApiResponse<AvatarResponse>>(`/accounts/me/avatar/${avatarId}/restore`),
}
