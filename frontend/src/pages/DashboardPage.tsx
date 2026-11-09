import { Link } from 'react-router-dom'
import { Users, Building2, FileText, ArrowUpRight } from 'lucide-react'
import { useAuthStore } from '@/stores/useAuthStore'

export default function DashboardPage() {
  const { user } = useAuthStore()

  const displayName = user
    ? user.firstName && user.lastName
      ? `${user.lastName} ${user.firstName}`
      : user.username
    : 'Admin'

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="p-8 rounded-3xl bg-gradient-to-r from-[#423223] to-[#5B4632] text-white shadow-lg relative overflow-hidden">
        <div className="relative z-10 max-w-xl">
          <h2 className="text-2xl font-bold tracking-tight mb-2">
            Xin chào, {displayName}! 👋
          </h2>
          <p className="text-xs text-[#E8DBC5]/80 leading-relaxed mb-5">
            Chào mừng bạn quay trở lại với Hệ thống Quản trị TestingSystem. Bạn có toàn quyền quản lý tài khoản, phòng ban.
          </p>
          <div className="flex items-center gap-3">
            <Link
              to="/accounts"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#C8792C] hover:bg-[#B26822] text-white font-bold text-xs shadow-md transition"
            >
              <span>Quản lý Tài Khoản</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
            <Link
              to="/departments"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white font-semibold text-xs border border-white/20 transition"
            >
              <span>Phòng Ban</span>
            </Link>
          </div>
        </div>
      </div>

      {/* 3 Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="p-6 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">Người dùng hệ thống</span>
            <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#5B4632]">Tài khoản</span>
            <span className="text-xs font-semibold text-[#C8792C]">Xem danh sách</span>
          </div>
        </div>

        <div className="p-6 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">Phòng ban hoạt động</span>
            <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#5B4632]">Phòng ban</span>
            <span className="text-xs font-semibold text-[#C8792C]">Quản lý tổ chức</span>
          </div>
        </div>
      </div>
    </div>
  )
}
