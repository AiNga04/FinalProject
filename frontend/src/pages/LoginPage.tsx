import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Lock, User, Sparkles, AlertCircle } from 'lucide-react'
import { useAuthStore } from '@/stores/useAuthStore'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { login } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from?.pathname || '/dashboard'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)
    setIsSubmitting(true)

    try {
      await login({ username, password })
      navigate(from, { replace: true })
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const errorResponse = err as { response?: { data?: { message?: string } } }
        setErrorMessage(errorResponse.response?.data?.message || 'Tên đăng nhập hoặc mật khẩu không chính xác')
      } else {
        setErrorMessage('Không thể kết nối đến máy chủ. Vui lòng thử lại!')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#FAF6EE] p-4 text-[#3D2D20]">
      <div className="w-full max-w-md bg-white rounded-3xl border border-[#E8DBC5] shadow-xl p-8 animate-in fade-in zoom-in-95 duration-200">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-[#C8792C] text-white flex items-center justify-center font-black text-2xl shadow-lg shadow-[#C8792C]/30 mx-auto mb-3">
            TS
          </div>
          <div className="flex items-center justify-center gap-1.5 mb-1">
            <h1 className="font-extrabold text-2xl text-[#5B4632] tracking-tight">TestingSystem</h1>
            <span className="w-2 h-2 rounded-full bg-[#C8792C]"></span>
          </div>
          <p className="text-xs text-[#5B4632]/70 font-medium">
            Cổng Quản Trị Hệ Thống Đào Tạo & Khảo Thí
          </p>
        </div>

        {/* Error Alert */}
        {errorMessage && (
          <div className="mb-5 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2.5">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} autoComplete="off" className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Tên đăng nhập
            </label>
            <div className="relative">
              <User className="w-4 h-4 text-[#5B4632]/50 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                autoComplete="username"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Ví dụ: admin hoặc employee"
                className="w-full pl-10 pr-4 py-2.5 text-xs rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C] transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-[#5B4632] mb-1.5">
              Mật khẩu
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-[#5B4632]/50 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 text-xs rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C] transition"
              />
            </div>
          </div>

          <div className="flex items-center justify-between text-xs pt-1">
            <span className="text-[#5B4632]/60">Refresh Token lưu HttpOnly Cookie</span>
            <span className="text-emerald-700 font-medium flex items-center gap-1">
              <Sparkles className="w-3.5 h-3.5" />
              Bảo mật XSS/CSRF
            </span>
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full mt-2 py-3 px-4 rounded-xl font-bold text-xs text-white bg-[#C8792C] hover:bg-[#B26822] shadow-md shadow-[#C8792C]/30 transition duration-150 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {isSubmitting ? (
              <span>Đang xác thực...</span>
            ) : (
              <span>Đăng Nhập</span>
            )}
          </button>
        </form>
      </div>
    </div>
  )
}

