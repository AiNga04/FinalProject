import { Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from '@/layouts/MainLayout'
import ProtectedRoute from '@/routes/ProtectedRoute'
import LoginPage from '@/pages/LoginPage'
import DashboardPage from '@/pages/DashboardPage'
import AccountPage from '@/pages/AccountPage'
import DepartmentPage from '@/pages/DepartmentPage'
import NotFound from '@/layouts/NotFound'
import Forbiddent from '@/layouts/Forbiddent'

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<Forbiddent />} />

      {/* Protected Routes (Bắt buộc đã đăng nhập) */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          
          {/* Admin Only Routes */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/accounts" element={<AccountPage />} />
            {/* DepartmentPage tạm thời dùng placeholder hoặc AccountPage */}
          <Route path="/departments" element={<DepartmentPage />} />
          </Route>
        </Route>
      </Route>

      {/* 404 Route */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

