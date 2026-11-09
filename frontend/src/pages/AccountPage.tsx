import { useState, useEffect, useCallback } from 'react'
import {
  Users,
  Search,
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
  X,
  Loader2,
  RefreshCw,
} from 'lucide-react'
import { accountApi } from '@/api/accountApi'
import type { Account, AccountType, Status } from '@/types/account'

export default function AccountPage() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [page, setPage] = useState(1) // Backend is 1-indexed for pageNumber
  const [pageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(1)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // Filters
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')
  const [selectedRole, setSelectedRole] = useState<string>('ALL')

  // Modal Create
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    accountType: 'USER' as AccountType,
    departmentId: 1,
  })

  // Fetch accounts from Spring Boot API
  const fetchAccounts = useCallback(async () => {
    setIsLoading(true)
    setErrorMessage(null)
    try {
      const res = await accountApi.getAccounts({
        pageNumber: page,
        size: pageSize,
        search: searchTerm || undefined,
        status: selectedStatus !== 'ALL' ? (selectedStatus as Status) : undefined,
        accountType: selectedRole !== 'ALL' ? (selectedRole as AccountType) : undefined,
      })

      const pageData = res.data
      setAccounts(pageData.items)
      setTotalElements(pageData.totalElements)
      setTotalPages(pageData.totalPages || 1)
    } catch {
      setErrorMessage('Không thể tải danh sách tài khoản từ máy chủ Spring Boot. Hãy chắc chắn Backend đang chạy (:8080)!')
    } finally {
      setIsLoading(false)
    }
  }, [page, pageSize, searchTerm, selectedStatus, selectedRole])

  useEffect(() => {
    fetchAccounts()
  }, [fetchAccounts])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await accountApi.createAccount(formData)
      setIsModalOpen(false)
      setFormData({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        accountType: 'USER',
        departmentId: 1,
      })
      fetchAccounts()
    } catch (err: unknown) {
      alert('Tạo tài khoản thất bại: ' + (err instanceof Error ? err.message : 'Lỗi hệ thống'))
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa tài khoản này?')) return
    try {
      await accountApi.updateStatus(id, 'DELETED')
      fetchAccounts()
    } catch {
      alert('Xóa tài khoản thất bại!')
    }
  }

  return (
    <div className="space-y-6">
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
          <h2 className="text-2xl font-bold text-[#5B4632] tracking-tight">Quản lý Tài Khoản</h2>
          <p className="text-xs text-[#5B4632]/70 mt-0.5">
            Danh sách tài khoản kết nối trực tiếp với Spring Boot REST API (`/api/v1/accounts`).
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => fetchAccounts()}
            title="Làm mới"
            className="p-2.5 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] transition cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin text-[#C8792C]' : ''}`} />
          </button>

          <button
            type="button"
            className="flex items-center gap-2 px-4 py-2.5 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] shadow-xs transition cursor-pointer"
          >
            <Download className="w-4 h-4 text-[#5B4632]/70" />
            <span>Xuất Excel</span>
          </button>

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-5 py-2.5 text-xs font-bold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-xl shadow-md shadow-[#C8792C]/30 transition hover:scale-[1.02] active:scale-95 cursor-pointer"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>Thêm Tài Khoản</span>
          </button>
        </div>
      </div>

      {/* Error notification banner if backend is not reachable */}
      {errorMessage && (
        <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-800 text-xs flex items-center justify-between">
          <span>{errorMessage}</span>
          <button
            type="button"
            onClick={() => fetchAccounts()}
            className="font-bold underline text-[#C8792C] ml-3 cursor-pointer"
          >
            Thử lại
          </button>
        </div>
      )}

      {/* 3 Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">Tổng tài khoản (Server)</span>
            <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#5B4632]">{totalElements}</span>
            <span className="text-xs font-semibold text-[#C8792C]">Bản ghi</span>
          </div>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">Trang hiện tại</span>
            <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-200">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#5B4632]">{page} / {totalPages}</span>
            <span className="text-xs font-medium text-emerald-700">{accounts.length} items/trang</span>
          </div>
        </div>

        <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">Xác thực API</span>
            <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#C8792C] border border-[#E8DBC5]">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold text-[#5B4632]">Bearer JWT</span>
            <span className="text-xs font-semibold text-emerald-700">Memory + Cookie</span>
          </div>
        </div>
      </div>

      {/* DATA TABLE CONTAINER */}
      <div className="rounded-2xl bg-white border border-[#E8DBC5] shadow-xs overflow-hidden">
        {/* Filter Bar */}
        <div className="p-4 border-b border-[#E8DBC5] bg-[#FAF6EE]/50 flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-xs font-semibold text-[#5B4632]">Lọc nhanh:</span>

            {/* Search Input */}
            <div className="relative w-60">
              <Search className="w-3.5 h-3.5 text-[#5B4632]/50 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Tìm username, email, tên..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value)
                  setPage(1)
                }}
                className="w-full pl-8 pr-3 py-1.5 text-xs rounded-xl bg-white border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C]"
              />
            </div>

            {/* Filter Role */}
            <div className="relative">
              <select
                value={selectedRole}
                onChange={(e) => {
                  setSelectedRole(e.target.value)
                  setPage(1)
                }}
                className="text-xs bg-white border border-[#E8DBC5] rounded-xl pl-3 pr-8 py-2 text-[#3D2D20] font-medium outline-none appearance-none focus:border-[#C8792C] cursor-pointer"
              >
                <option value="ALL">Tất cả vai trò</option>
                <option value="ADMIN">ADMIN</option>
                <option value="MANAGER">MANAGER</option>
                <option value="EMPLOYEE">EMPLOYEE</option>
                <option value="USER">USER</option>
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-[#5B4632]/60 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>

            {/* Filter Trạng thái */}
            <div className="relative">
              <select
                value={selectedStatus}
                onChange={(e) => {
                  setSelectedStatus(e.target.value)
                  setPage(1)
                }}
                className="text-xs bg-white border border-[#E8DBC5] rounded-xl pl-3 pr-8 py-2 text-[#3D2D20] font-medium outline-none appearance-none focus:border-[#C8792C] cursor-pointer"
              >
                <option value="ALL">Tất cả trạng thái</option>
                <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                <option value="PENDING">Chờ duyệt (PENDING)</option>
                <option value="BLOCKED">Đã khóa (BLOCKED)</option>
                <option value="DELETED">Đã xóa (DELETED)</option>
              </select>
              <ChevronDown className="w-3.5 h-3.5 text-[#5B4632]/60 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          <span className="text-xs text-[#5B4632]/70 font-medium">
            Tổng cộng: <strong className="text-[#C8792C]">{totalElements}</strong> tài khoản
          </span>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#FAF6EE] text-[#5B4632] uppercase tracking-wider border-b border-[#E8DBC5] font-bold">
              <tr>
                <th className="py-3.5 px-6">ID</th>
                <th className="py-3.5 px-6">Tài Khoản & Tên</th>
                <th className="py-3.5 px-6">Email</th>
                <th className="py-3.5 px-6">Vai Trò</th>
                <th className="py-3.5 px-6">Trạng Thái</th>
                <th className="py-3.5 px-6">Ngày Tạo</th>
                <th className="py-3.5 px-6 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E8DBC5]/50">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-[#5B4632]">
                    <div className="flex items-center justify-center gap-2">
                      <Loader2 className="w-5 h-5 animate-spin text-[#C8792C]" />
                      <span className="text-xs font-semibold">Đang tải dữ liệu từ Spring Boot...</span>
                    </div>
                  </td>
                </tr>
              ) : accounts.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-[#5B4632]/50">
                    Chưa có tài khoản nào hoặc không tìm thấy kết quả phù hợp.
                  </td>
                </tr>
              ) : (
                accounts.map((acc) => (
                  <tr key={acc.id} className="hover:bg-[#FAF6EE]/60 transition">
                    <td className="py-4 px-6 font-mono text-[#5B4632]/60">#{acc.id}</td>

                    <td className="py-4 px-6">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-[#FAF6EE] border border-[#E8DBC5] text-[#5B4632] font-bold flex items-center justify-center text-xs">
                          {acc.username.charAt(0).toUpperCase()}
                        </div>
                        <div>
                          <p className="font-bold text-[#5B4632]">{acc.username}</p>
                          <p className="text-[11px] text-[#5B4632]/70">
                            {acc.lastName} {acc.firstName}
                          </p>
                        </div>
                      </div>
                    </td>

                    <td className="py-4 px-6 font-mono text-[#5B4632]/80">{acc.email}</td>

                    <td className="py-4 px-6">
                      <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
                        <ShieldCheck className="w-3.5 h-3.5 text-[#C8792C]" />
                        {acc.accountType}
                      </span>
                    </td>

                    <td className="py-4 px-6">
                      {acc.status === 'ACTIVE' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200">
                          <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                          Hoạt động
                        </span>
                      )}
                      {acc.status === 'PENDING' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-50 text-amber-800 border border-amber-200">
                          <Clock className="w-3 h-3 text-amber-600" />
                          Chờ duyệt
                        </span>
                      )}
                      {acc.status === 'BLOCKED' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-50 text-rose-800 border border-rose-200">
                          <XCircle className="w-3 h-3 text-rose-600" />
                          Đã khóa
                        </span>
                      )}
                      {acc.status === 'DELETED' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-zinc-100 text-zinc-600 border border-zinc-200">
                          Đã xóa
                        </span>
                      )}
                    </td>

                    <td className="py-4 px-6 font-mono text-[11px] text-[#5B4632]/70">
                      {acc.createdAt ? new Date(acc.createdAt).toLocaleDateString('vi-VN') : '—'}
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
                          onClick={() => handleDelete(acc.id)}
                          title="Xóa tài khoản"
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
          <span>
            Trang {page} / {totalPages} (Tổng số {totalElements} tài khoản)
          </span>
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              disabled={page <= 1}
              onClick={() => setPage((p) => Math.max(p - 1, 1))}
              className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
            >
              Trước
            </button>
            <span className="px-3.5 py-1.5 rounded-lg bg-[#C8792C] text-white font-bold shadow-xs">
              {page}
            </span>
            <button
              type="button"
              disabled={page >= totalPages}
              onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
              className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] bg-white text-[#5B4632] hover:bg-[#FAF6EE] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
            >
              Sau
            </button>
          </div>
        </div>
      </div>

      {/* MODAL CREATE ACCOUNT */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-md w-full p-6 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between pb-4 border-b border-[#E8DBC5]">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-xl bg-[#FAF6EE] text-[#C8792C]">
                  <Sparkles className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-base text-[#5B4632]">Tạo Tài Khoản Mới</h3>
              </div>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="p-1 rounded-lg text-[#5B4632]/60 hover:text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="mt-5 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Tên đăng nhập (Username)</label>
                <input
                  type="text"
                  required
                  placeholder="admin02"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  className="w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Mật khẩu</label>
                <input
                  type="password"
                  required
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  className="w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Email</label>
                <input
                  type="email"
                  required
                  placeholder="user@example.com"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Họ (Last Name)</label>
                  <input
                    type="text"
                    required
                    placeholder="Nguyễn"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    className="w-full text-xs px-3 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Tên (First Name)</label>
                  <input
                    type="text"
                    required
                    placeholder="Văn An"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    className="w-full text-xs px-3 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#5B4632] mb-1.5">Vai trò (Role)</label>
                <select
                  value={formData.accountType}
                  onChange={(e) => setFormData({ ...formData, accountType: e.target.value as AccountType })}
                  className="w-full text-xs px-3 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]"
                >
                  <option value="USER">USER</option>
                  <option value="EMPLOYEE">EMPLOYEE</option>
                  <option value="MANAGER">MANAGER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
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
                  Tạo Tài Khoản
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
