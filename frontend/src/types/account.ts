import type { PaginationParams } from "./api"

export type AccountType = 'ADMIN' | 'MANAGER' | 'EMPLOYEE' | 'USER'
export type Status = 'ACTIVE' | 'INACTIVE' | 'DELETED'

export interface Account {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  accountType: AccountType
  status: Status
  departmentId?: number | undefined
  departmentName?: string
  createdAt: string
}

export interface AccountSearchParams extends PaginationParams {
  search?: string | undefined // Tìm username, email, name
  accountType?: AccountType | undefined
  status?: Status | undefined
  departmentId?: number | undefined
  minCreatedDate?: string | undefined
  maxCreatedDate?: string | undefined
}

export interface CreateAccountRequest {
  username: string
  email: string
  password?: string | undefined
  firstName: string
  lastName: string
  accountType: AccountType
  departmentId?: number | undefined
}

export interface UpdateAccountRequest {
  username?: string | undefined
  email?: string | undefined
  firstName?: string | undefined
  lastName?: string | undefined
  accountType?: AccountType | undefined
  departmentId?: number | undefined
}
