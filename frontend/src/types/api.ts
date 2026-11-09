export interface ApiResponse<T> {
  message: string
  data: T
  errors?: Record<string, string>
  timestamp: string
}

// Cấu trúc phân trang khớp với PageResponse backend
export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

// Phân trang gửi lên Backend
export interface PaginationParams {
  pageNumber?: number | undefined // Mặc định từ 1
  size?: number | undefined // Mặc định 10
  sort?: string | undefined
}
