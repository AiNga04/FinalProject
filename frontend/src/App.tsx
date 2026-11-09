import { useState, useMemo } from 'react'
import {
  Users,
  Building2,
  FileText,
  BarChart3,
  Settings,
  Search,
  Bell,
  Plus,
  Download,
  Edit,
  Trash2,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  Clock,
  Sparkles,
  ChevronDown,
  LogOut,
  X,
} from 'lucide-react'

interface UserItem {
  id: number
  name: string
  email: string
  department: string
  role: 'ADMIN' | 'MANAGER' | 'USER'
  status: 'ACTIVE' | 'PENDING' | 'BLOCKED'
  createdAt: string
  avatar: string
}

const INITIAL_USERS: UserItem[] = [
  {
    id: 1,
    name: 'Trương Ái Nga',
    email: 'nga.truong@testingsystem.com',
    department: 'Ban Giám Đốc',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: '01/10/2026',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop&crop=faces',
  },
  {
    id: 2,
    name: 'Trần Thị Mai',
    email: 'mai.tran@testingsystem.com',
    department: 'Phòng Kỹ Thuật (IT)',
    role: 'MANAGER',
    status: 'ACTIVE',
    createdAt: '15/10/2026',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&crop=faces',
  },
  {
    id: 3,
    name: 'Lê Hoàng Nam',
    email: 'nam.le@testingsystem.com',
    department: 'Phòng Đào Tạo',
    role: 'USER',
    status: 'PENDING',
    createdAt: '02/11/2026',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=faces',
  },
  {
    id: 4,
    name: 'Phạm Thu Hương',
    email: 'huong.pham@testingsystem.com',
    department: 'Phòng Nhân Sự',
    role: 'USER',
    status: 'BLOCKED',
    createdAt: '28/09/2026',
    avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&crop=faces',
  },
  {
    id: 5,
    name: 'Đặng Minh Khôi',
    email: 'khoi.dang@testingsystem.com',
    department: 'Phòng Kỹ Thuật (IT)',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: '05/11/2026',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop&crop=faces',
  },
]

export default function App() {
  const [users, setUsers] = useState<UserItem[]>(INITIAL_USERS)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedDept, setSelectedDept] = useState('ALL')
  const [selectedStatus, setSelectedStatus] = useState('ALL')
  const [isModalOpen, setIsModalOpen] = useState(false)

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    department: 'Phòng Kỹ Thuật (IT)',
    role: 'USER' as 'ADMIN' | 'MANAGER' | 'USER',
  })

  // Filtered list
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchSearch =
        u.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.department.toLowerCase().includes(searchTerm.toLowerCase())

      const matchDept = selectedDept === 'ALL' || u.department === selectedDept
      const matchStatus = selectedStatus === 'ALL' || u.status === selectedStatus

      return matchSearch && matchDept && matchStatus
    })
  }, [users, searchTerm, selectedDept, selectedStatus])

  // Stats
  const totalCount = users.length
  const activeCount = users.filter((u) => u.status === 'ACTIVE').length
  const pendingCount = users.filter((u) => u.status === 'PENDING').length

  const handleCreateUser = (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.name || !formData.email) return

    const newUser: UserItem = {
      id: Date.now(),
      name: formData.name,
      email: formData.email,
      department: formData.department,
      role: formData.role,
      status: 'ACTIVE',
      createdAt: 'Hôm nay',
      avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop&crop=faces',
    }

    setUsers([newUser, ...users])
    setFormData({ name: '', email: '', department: 'Phòng Kỹ Thuật (IT)', role: 'USER' })
    setIsModalOpen(false)
  }

  const handleDeleteUser = (id: number) => {
    if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
      setUsers(users.filter((u) => u.id !== id))
    }
  }

  return (
    <div className="min-h-screen flex bg-[#FAF6EE] text-[#3D2D20] font-sans antialiased">
      {/* 🌲 SIDEBAR - Nâu Gỗ Trầm Hội An (#423223 / #5B4632) */}
      <aside className="w-64 flex-shrink-0 bg-[#423223] text-[#E8DBC5] flex flex-col justify-between border-r border-[#35271A] shadow-xl">
        <div>
          {/* Brand Logo Header */}
          <div className="h-20 flex items-center gap-3 px-6 border-b border-white/10">
            <div className="w-10 h-10 rounded-2xl bg-[#C8792C] text-white flex items-center justify-center font-bold text-lg shadow-md shadow-[#C8792C]/30">
              TS
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <h1 className="font-bold text-white text-base tracking-tight leading-none">TestingSystem</h1>
                <span className="w-1.5 h-1.5 rounded-full bg-[#C8792C]"></span>
              </div>
              <span className="text-[10px] text-[#F4D9A0]/70 font-medium tracking-widest uppercase">
                Hội An Ikigai Theme
              </span>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="p-4 space-y-1.5">
            <a
              href="#dashboard"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-[#E8DBC5]/80 hover:text-white hover:bg-white/5 transition"
            >
              <BarChart3 className="w-4 h-4 text-[#F4D9A0]/60" />
              <span>Tổng quan</span>
            </a>

            {/* Active Link: Quản lý Người dùng (Nổi bật với Cam Đất Nung #C8792C) */}
            <a
              href="#users"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-white bg-[#C8792C] shadow-md shadow-[#C8792C]/25 transition"
            >
              <Users className="w-4 h-4 text-white" />
              <span>Quản lý Người dùng</span>
            </a>

            <a
              href="#departments"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-[#E8DBC5]/80 hover:text-white hover:bg-white/5 transition"
            >
              <Building2 className="w-4 h-4 text-[#F4D9A0]/60" />
              <span>Phòng ban (Department)</span>
            </a>

            <a
              href="#exams"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-[#E8DBC5]/80 hover:text-white hover:bg-white/5 transition"
            >
              <FileText className="w-4 h-4 text-[#F4D9A0]/60" />
              <span>Đề thi & Kiểm tra</span>
            </a>

            <a
              href="#settings"
              className="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-medium text-[#E8DBC5]/80 hover:text-white hover:bg-white/5 transition"
            >
              <Settings className="w-4 h-4 text-[#F4D9A0]/60" />
              <span>Cấu hình hệ thống</span>
            </a>
          </nav>
        </div>

        {/* User Card at Bottom of Sidebar */}
        <div className="p-4 border-t border-white/10">
          <div className="flex items-center justify-between p-2.5 rounded-xl bg-white/5">
            <div className="flex items-center gap-3 min-w-0">
              <img
                src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80&h=80&fit=crop&crop=faces"
                alt="Avatar"
                className="w-9 h-9 rounded-xl object-cover ring-2 ring-[#C8792C]/50"
              />
              <div className="truncate">
                <p className="text-xs font-bold text-white truncate">Trương Ái Nga</p>
                <p className="text-[10px] text-[#F4D9A0]/70 truncate">Super Admin</p>
              </div>
            </div>
            <button
              type="button"
              title="Đăng xuất"
              className="p-1.5 rounded-lg text-[#E8DBC5]/60 hover:text-white hover:bg-white/10 transition cursor-pointer"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* 📄 MAIN CONTENT AREA */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        {/* Top Navbar */}
        <header className="h-20 bg-white border-b border-[#E8DBC5] px-8 flex items-center justify-between sticky top-0 z-30 shadow-xs">
          {/* Search Box */}
          <div className="relative w-96">
            <Search className="w-4 h-4 text-[#5B4632]/50 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tìm kiếm người dùng theo tên, email, phòng ban..."
              className="w-full pl-10 pr-4 py-2 text-xs rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C] transition"
            />
          </div>

          {/* Right Header Status & Notification */}
          <div className="flex items-center gap-4">
            <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#FAF6EE] border border-[#E8DBC5] text-xs font-medium text-[#5B4632]">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span>Spring Boot: Online (:8080)</span>
            </div>

            <button
              type="button"
              className="relative p-2.5 rounded-xl border border-[#E8DBC5] text-[#5B4632] hover:bg-[#FAF6EE] transition cursor-pointer"
            >
              <Bell className="w-4 h-4" />
              <span className="w-2 h-2 rounded-full bg-[#C8792C] absolute top-2 right-2"></span>
            </button>
          </div>
        </header>

        {/* Content Body */}
        <main className="p-8 space-y-6 max-w-7xl mx-auto w-full">
          {/* Header Banner & Main Actions */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-semibold uppercase tracking-wider text-[#C8792C]">
                  TestingSystem Portal
                </span>
                <span className="text-xs text-[#5B4632]/40">•</span>
                <span className="text-xs text-[#5B4632]/70">Quản trị viên</span>
              </div>
              <h2 className="text-2xl font-bold text-[#5B4632] tracking-tight">Quản lý Người dùng</h2>
              <p className="text-xs text-[#5B4632]/70 mt-0.5">
                Quản lý tài khoản, phòng ban và phân quyền truy cập trong hệ thống bài thi.
              </p>
            </div>

            <div className="flex items-center gap-3">
              <button
                type="button"
                className="flex items-center gap-2 px-4 py-2.5 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] shadow-xs transition cursor-pointer"
              >
                <Download className="w-4 h-4 text-[#5B4632]/70" />
                <span>Xuất Excel</span>
              </button>

              {/* Nút Thêm Người Dùng (Màu Cam Đất Nung Hội An #C8792C) */}
              <button
                type="button"
                onClick={() => setIsModalOpen(true)}
                className="flex items-center gap-2 px-5 py-2.5 text-xs font-bold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-xl shadow-md shadow-[#C8792C]/30 transition hover:scale-[1.02] active:scale-95 cursor-pointer"
              >
                <Plus className="w-4 h-4 stroke-[3]" />
                <span>Thêm Người Dùng</span>
              </button>
            </div>
          </div>

          {/* 3 STATS CARDS (Phong cách Hội An Ikigai dịu mát) */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {/* Card 1: Tổng người dùng */}
            <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-[#5B4632]/70">Tổng số người dùng</span>
                <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
                  <Users className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-3 flex items-baseline gap-2">
                <span className="text-2xl font-bold text-[#5B4632]">{totalCount}</span>
                <span className="text-xs font-semibold text-[#C8792C]">Tài khoản hệ thống</span>
              </div>
            </div>

            {/* Card 2: Đang hoạt động */}
            <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-[#5B4632]/70">Đang hoạt động (ACTIVE)</span>
                <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-200">
                  <CheckCircle2 className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-3 flex items-baseline gap-2">
                <span className="text-2xl font-bold text-[#5B4632]">{activeCount}</span>
                <span className="text-xs font-medium text-emerald-700">
                  {Math.round((activeCount / (totalCount || 1)) * 100)}% tổng số
                </span>
              </div>
            </div>

            {/* Card 3: Chờ duyệt */}
            <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-[#5B4632]/70">Chờ kích hoạt / Duyệt</span>
                <div className="p-2.5 rounded-xl bg-amber-50 text-amber-700 border border-amber-200">
                  <Clock className="w-4 h-4" />
                </div>
              </div>
              <div className="mt-3 flex items-baseline gap-2">
                <span className="text-2xl font-bold text-[#5B4632]">{pendingCount}</span>
                <span className="text-xs font-medium text-amber-700">Cần xác nhận</span>
              </div>
            </div>
          </div>

          {/* DATA TABLE CONTAINER */}
          <div className="rounded-2xl bg-white border border-[#E8DBC5] shadow-xs overflow-hidden">
            {/* Filter Bar */}
            <div className="p-4 border-b border-[#E8DBC5] bg-[#FAF6EE]/50 flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap items-center gap-3">
                <span className="text-xs font-semibold text-[#5B4632]">Lọc theo:</span>

                {/* Filter Phòng ban */}
                <div className="relative">
                  <select
                    value={selectedDept}
                    onChange={(e) => setSelectedDept(e.target.value)}
                    className="text-xs bg-white border border-[#E8DBC5] rounded-xl pl-3 pr-8 py-2 text-[#3D2D20] font-medium outline-none appearance-none focus:border-[#C8792C] cursor-pointer"
                  >
                    <option value="ALL">Tất cả phòng ban</option>
                    <option value="Ban Giám Đốc">Ban Giám Đốc</option>
                    <option value="Phòng Kỹ Thuật (IT)">Phòng Kỹ Thuật (IT)</option>
                    <option value="Phòng Đào Tạo">Phòng Đào Tạo</option>
                    <option value="Phòng Nhân Sự">Phòng Nhân Sự</option>
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-[#5B4632]/60 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>

                {/* Filter Trạng thái */}
                <div className="relative">
                  <select
                    value={selectedStatus}
                    onChange={(e) => setSelectedStatus(e.target.value)}
                    className="text-xs bg-white border border-[#E8DBC5] rounded-xl pl-3 pr-8 py-2 text-[#3D2D20] font-medium outline-none appearance-none focus:border-[#C8792C] cursor-pointer"
                  >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                    <option value="PENDING">Chờ duyệt (PENDING)</option>
                    <option value="BLOCKED">Đã khóa (BLOCKED)</option>
                  </select>
                  <ChevronDown className="w-3.5 h-3.5 text-[#5B4632]/60 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                </div>
              </div>

              <span className="text-xs text-[#5B4632]/70 font-medium">
                Tìm thấy <strong className="text-[#C8792C]">{filteredUsers.length}</strong> kết quả
              </span>
            </div>

            {/* Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#FAF6EE] text-[#5B4632] uppercase tracking-wider border-b border-[#E8DBC5] font-bold">
                  <tr>
                    <th className="py-3.5 px-6">ID</th>
                    <th className="py-3.5 px-6">Họ & Tên</th>
                    <th className="py-3.5 px-6">Phòng Ban</th>
                    <th className="py-3.5 px-6">Vai Trò</th>
                    <th className="py-3.5 px-6">Trạng Thái</th>
                    <th className="py-3.5 px-6">Ngày Tạo</th>
                    <th className="py-3.5 px-6 text-right">Thao Tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#E8DBC5]/50">
                  {filteredUsers.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="text-center py-12 text-[#5B4632]/50">
                        Không tìm thấy người dùng nào phù hợp.
                      </td>
                    </tr>
                  ) : (
                    filteredUsers.map((user) => (
                      <tr key={user.id} className="hover:bg-[#FAF6EE]/60 transition">
                        <td className="py-4 px-6 font-mono text-[#5B4632]/60">#{user.id}</td>

                        <td className="py-4 px-6">
                          <div className="flex items-center gap-3">
                            <img
                              src={user.avatar}
                              alt={user.name}
                              className="w-9 h-9 rounded-full object-cover ring-1 ring-[#E8DBC5]"
                            />
                            <div>
                              <p className="font-bold text-[#5B4632]">{user.name}</p>
                              <p className="text-[11px] text-[#5B4632]/70">{user.email}</p>
                            </div>
                          </div>
                        </td>

                        <td className="py-4 px-6">
                          <span className="inline-flex items-center gap-1.5 font-medium text-[#3D2D20]">
                            <Building2 className="w-3.5 h-3.5 text-[#C8792C]" />
                            {user.department}
                          </span>
                        </td>

                        <td className="py-4 px-6">
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
                            <ShieldCheck className="w-3.5 h-3.5 text-[#C8792C]" />
                            {user.role}
                          </span>
                        </td>

                        <td className="py-4 px-6">
                          {user.status === 'ACTIVE' && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200">
                              <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                              Đang hoạt động
                            </span>
                          )}
                          {user.status === 'PENDING' && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-50 text-amber-800 border border-amber-200">
                              <Clock className="w-3 h-3 text-amber-600" />
                              Chờ duyệt
                            </span>
                          )}
                          {user.status === 'BLOCKED' && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-50 text-rose-800 border border-rose-200">
                              <XCircle className="w-3 h-3 text-rose-600" />
                              Đã khóa
                            </span>
                          )}
                        </td>

                        <td className="py-4 px-6 font-mono text-[11px] text-[#5B4632]/70">
                          {user.createdAt}
                        </td>

                        <td className="py-4 px-6 text-right">
                          <div className="inline-flex items-center gap-1">
                            <button
                              type="button"
                              title="Chỉnh sửa"
                              className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-[#C8792C] hover:bg-[#FAF6EE] transition cursor-pointer"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteUser(user.id)}
                              title="Xóa người dùng"
                              className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-rose-600 hover:bg-rose-50 transition cursor-pointer"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination Footer */}
            <div className="p-4 border-t border-[#E8DBC5] flex items-center justify-between text-xs text-[#5B4632]/80 bg-[#FAF6EE]/30">
              <span>Hiển thị {filteredUsers.length} trên tổng số {totalCount} người dùng</span>
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  disabled
                  className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632]/40 cursor-not-allowed"
                >
                  Trước
                </button>
                <button
                  type="button"
                  className="px-3.5 py-1.5 rounded-lg bg-[#C8792C] text-white font-bold shadow-xs cursor-pointer"
                >
                  1
                </button>
                <button
                  type="button"
                  className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
                >
                  2
                </button>
                <button
                  type="button"
                  className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
                >
                  Sau
                </button>
              </div>
            </div>
          </div>
        </main>
      </div>

      {/* 🌟 MODAL THÊM NGƯỜI DÙNG */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-md w-full p-6 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between pb-4 border-b border-[#E8DBC5]">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-xl bg-[#FAF6EE] text-[#C8792C]">
                  <Sparkles className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-base text-[#5B4632]">Thêm Người Dùng Mới</h3>
              </div>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="p-1 rounded-lg text-[#5B4632]/60 hover:text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateUser} className="mt-5 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Họ và Tên</label>
                <input
                  type="text"
                  required
                  placeholder="Ví dụ: Nguyễn Văn B"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Email</label>
                <input
                  type="email"
                  required
                  placeholder="example@testingsystem.com"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C]"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Phòng ban</label>
                  <select
                    value={formData.department}
                    onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                    className="w-full text-xs px-3 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                  >
                    <option value="Phòng Kỹ Thuật (IT)">Phòng Kỹ Thuật (IT)</option>
                    <option value="Ban Giám Đốc">Ban Giám Đốc</option>
                    <option value="Phòng Đào Tạo">Phòng Đào Tạo</option>
                    <option value="Phòng Nhân Sự">Phòng Nhân Sự</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Vai trò (Role)</label>
                  <select
                    value={formData.role}
                    onChange={(e) =>
                      setFormData({ ...formData, role: e.target.value as 'ADMIN' | 'MANAGER' | 'USER' })
                    }
                    className="w-full text-xs px-3 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                  >
                    <option value="USER">USER</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-[#E8DBC5]">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] cursor-pointer"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-xl shadow-md shadow-[#C8792C]/30 cursor-pointer"
                >
                  Lưu Người Dùng
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
