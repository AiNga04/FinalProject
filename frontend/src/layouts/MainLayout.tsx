import { NavLink, Outlet } from 'react-router-dom'
import {
  Users,
  Building2,
  BarChart3,
  LogOut,
  X,
} from 'lucide-react'
import { useState } from 'react'
import Header from './Header'
import { useAuthStore } from '@/stores/useAuthStore'

const NAV_ITEMS = [
  { path: '/dashboard', label: 'Tổng quan', icon: BarChart3 },
  { path: '/accounts', label: 'Quản lý Tài khoản', icon: Users },
  { path: '/departments', label: 'Phòng ban (Department)', icon: Building2 },
]

export default function MainLayout() {
  const { user, logout } = useAuthStore()
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  const displayName = user
    ? user.firstName && user.lastName
      ? `${user.lastName} ${user.firstName}`
      : user.username
    : 'Admin'

  const userRole = user?.accountType || 'ADMIN'

  return (
    <div className="h-screen overflow-hidden flex bg-[#FAF6EE] text-[#3D2D20] font-sans antialiased">
      {isSidebarOpen && (
        <button
          type="button"
          aria-label="Đóng menu"
          onClick={() => setIsSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-black/40 lg:hidden"
        />
      )}
      {/* 🌲 SIDEBAR - Nâu Gỗ Trầm Hội An (#423223 / #5B4632) */}
      <aside className={`fixed inset-y-0 left-0 z-50 h-dvh max-h-dvh w-72 flex-shrink-0 bg-[#423223] text-[#E8DBC5] flex flex-col overflow-hidden border-r border-[#35271A] shadow-xl transition-transform duration-200 lg:static lg:h-screen lg:max-h-screen lg:z-auto lg:w-64 lg:translate-x-0 ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="min-h-0 flex-1 overflow-hidden">
          {/* Logo Brand Header */}
          <div className="h-20 flex items-center gap-3 px-5 lg:px-6 border-b border-white/10">
            <div className="w-10 h-10 rounded-2xl bg-[#C8792C] text-white flex items-center justify-center font-bold text-lg shadow-md shadow-[#C8792C]/30">
              TS
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <h1 className="font-bold text-white text-base tracking-tight leading-none">TestingSystem</h1>
                <span className="w-1.5 h-1.5 rounded-full bg-[#C8792C]"></span>
              </div>
            </div>
            <button
              type="button"
              onClick={() => setIsSidebarOpen(false)}
              aria-label="Đóng menu"
              className="ml-auto lg:hidden p-2 rounded-lg text-[#E8DBC5]/70 hover:bg-white/10 hover:text-white cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Navigation Links */}
          <nav className="p-4 space-y-1.5">
            {NAV_ITEMS.map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition ${
                      isActive
                        ? 'text-white bg-[#C8792C] shadow-md shadow-[#C8792C]/25'
                        : 'text-[#E8DBC5]/80 hover:text-white hover:bg-white/5'
                    }`
                  }
                  onClick={() => setIsSidebarOpen(false)}
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </NavLink>
              )
            })}
          </nav>
        </div>

        {/* User Card & Logout at Bottom of Sidebar */}
        <div className="shrink-0 p-4 border-t border-white/10">
          <div className="flex items-center justify-between p-2.5 rounded-xl bg-white/5">
            <div className="flex items-center gap-3 min-w-0">
              <div className="w-9 h-9 rounded-xl bg-[#5B4632] border border-[#E8DBC5]/30 text-white font-bold flex items-center justify-center text-xs">
                {displayName.charAt(0).toUpperCase()}
              </div>
              <div className="truncate">
                <p className="text-xs font-bold text-white truncate">{displayName}</p>
                <p className="text-[10px] text-[#F4D9A0]/70 truncate">{userRole}</p>
              </div>
            </div>
            <button
              type="button"
              onClick={logout}
              title="Đăng xuất"
              className="p-1.5 rounded-lg text-[#E8DBC5]/60 hover:text-rose-400 hover:bg-white/10 transition cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* 📄 MAIN CONTENT AREA */}
      <div className="flex-1 flex flex-col min-w-0 min-h-0 overflow-hidden">
        <Header onMenuOpen={() => setIsSidebarOpen(true)} />
        <main className="min-h-0 flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div className="max-w-7xl mx-auto w-full">
          <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}

