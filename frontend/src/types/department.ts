import type { Status } from './account'
import type { PaginationParams } from './api'

export interface Department {
  id: number
  name: string
  type: string
  totalMember: number
  status: Status
  createdAt: string
  updatedAt?: string
}

export interface DepartmentSearchParams extends PaginationParams {
  search?: string | undefined
  type?: string | undefined
  status?: Status | undefined
}

export interface CreateDepartmentRequest {
  name: string
  type: string
}

export interface UpdateDepartmentRequest {
  name?: string | undefined
  type?: string | undefined
}