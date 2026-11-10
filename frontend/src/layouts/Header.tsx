import { Search, Bell, Menu } from 'lucide-react'
import { useAuthStore } from '@/stores/useAuthStore'

interface HeaderProps {
  searchTerm?: string
  onSearchChange?: (value: string) => void
  searchPlaceholder?: string
  onMenuOpen?: () => void
}

export default function Header({
  searchTerm = '',
  onSearchChange,
  searchPlaceholder = 'Tìm kiếm trong hệ thống...',
  onMenuOpen,
}: HeaderProps) {
  const { user } = useAuthStore()

  return (
    <header className="h-20 bg-white border-b border-[#E8DBC5] px-4 sm:px-6 lg:px-8 flex items-center gap-3 justify-between sticky top-0 z-30 shadow-xs">
      <button
        type="button"
        onClick={onMenuOpen}
        aria-label="Mở menu"
        className="lg:hidden shrink-0 p-2 rounded-xl border border-[#E8DBC5] text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
      >
        <Menu className="w-5 h-5" />
      </button>
      {/* Search Input Box */}
      <div className="relative flex-1 max-w-96">
        <Search className="w-4 h-4 text-[#5B4632]/50 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => onSearchChange?.(e.target.value)}
          placeholder={searchPlaceholder}
          className="w-full pl-10 pr-4 py-2 text-xs rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C] focus:ring-1 focus:ring-[#C8792C] transition"
        />
      </div>

      {/* Right Header Status & Notification */}
      <div className="flex items-center gap-4">
        <button
          type="button"
          title="Thông báo"
          className="relative p-2.5 rounded-xl border border-[#E8DBC5] text-[#5B4632] hover:bg-[#FAF6EE] transition cursor-pointer"
        >
          <Bell className="w-4 h-4" />
          <span className="w-2 h-2 rounded-full bg-[#C8792C] absolute top-2 right-2"></span>
        </button>

        {/* User Badge */}
        {user && (
          <div className="flex items-center gap-2 pl-2 border-l border-[#E8DBC5]">
            <span className="text-xs font-bold text-[#5B4632]">
              <span className="hidden sm:inline">{user.firstName ? `${user.lastName} ${user.firstName}` : user.username}</span>
            </span>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#C8792C]/10 text-[#C8792C] font-mono font-bold border border-[#C8792C]/30">
              {user.accountType}
            </span>
          </div>
        )}
      </div>
    </header>
  )
}

