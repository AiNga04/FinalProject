import axiosClient from './axiosClient'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
  Department,
  DepartmentSearchParams,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
} from '@/types/department'
import type { Status } from '@/types/account'

export const departmentApi = {
  // Lấy danh sách phòng ban (phân trang, tìm kiếm)
  getDepartments: (params?: DepartmentSearchParams) => {
    return axiosClient.get<unknown, ApiResponse<PageResponse<Department>>>('/departments', { params })
  },

  // Lấy chi tiết phòng ban theo ID
  getDepartmentById: (id: number) => {
    return axiosClient.get<unknown, ApiResponse<Department>>(`/departments/${id}`)
  },

  // Tạo phòng ban mới
  createDepartment: (data: CreateDepartmentRequest) => {
    return axiosClient.post<unknown, ApiResponse<Department>>('/departments', data)
  },

  // Cập nhật thông tin phòng ban
  updateDepartment: (id: number, data: UpdateDepartmentRequest) => {
    return axiosClient.put<unknown, ApiResponse<Department>>(`/departments/${id}`, data)
  },

  // Đổi trạng thái 1 phòng ban
  updateStatus: (id: number, status: Status) => {
    return axiosClient.patch<unknown, ApiResponse<Department>>(`/departments/${id}/status`, { status })
  },

  // Đổi trạng thái nhiều phòng ban
  updateStatusList: (ids: number[], status: Status) => {
    return axiosClient.patch<unknown, ApiResponse<Department[]>>('/departments/updateStatusList', {
      ids,
      status,
    })
  },
}

