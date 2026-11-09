import type { PaginationParams } from "./api"

export type AccountType = 'ADMIN' | 'MANAGER' | 'EMPLOYEE' | 'USER'
export type Status = 'ACTIVE' | 'PENDING' | 'BLOCKED' | 'DELETED'

export interface Account {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  accountType: AccountType
  status: Status
  departmentId?: number
  departmentName?: string
  createdAt: string
}

export interface AccountSearchParams extends PaginationParams {
  search?: string | undefined // Tìm username, email, name
  accountType?: AccountType | undefined
  status?: Status | undefined
  departmentId?: number | undefined
}

export interface CreateAccountRequest {
  username: string
  email: string
  password?: string | undefined
  firstName: string
  lastName: string
  accountType: AccountType
  departmentId: number
}

export interface UpdateAccountRequest {
  email?: string | undefined
  firstName?: string | undefined
  lastName?: string | undefined
  accountType?: AccountType | undefined
  departmentId?: number | undefined
}