import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/useAuthStore'
import type { AccountType } from '@/types/account'

interface ProtectedRouteProps {
  allowedRoles?: AccountType[]
}

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, user } = useAuthStore()
  const location = useLocation()

  // 1. Màn hình chờ khi đang F5 / kiểm tra HttpOnly cookie ngầm
  if (isLoading) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-[#FAF6EE] text-[#5B4632]">
        <div className="w-12 h-12 rounded-2xl bg-[#C8792C] text-white flex items-center justify-center font-bold text-xl shadow-lg shadow-[#C8792C]/30 animate-bounce mb-3">
          TS
        </div>
        <p className="text-xs font-semibold tracking-wider text-[#5B4632]/70 uppercase animate-pulse">
          Đang xác thực hệ thống...
        </p>
      </div>
    )
  }

  // 2. Chưa đăng nhập -> Chuyển về trang /login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // 3. Đã đăng nhập nhưng không đủ quyền (Role)
  if (allowedRoles && user && !allowedRoles.includes(user.accountType)) {
    return <Navigate to="/forbidden" replace />
  }

  // 4. Hợp lệ -> Cho phép truy cập
  return <Outlet />
}

