import { Users, CheckCircle2, Trash2 } from 'lucide-react'
import type { Account } from '@/types/account'

interface AccountStatsBarProps {
  totalElements: number
  accounts: Account[]
}

export default function AccountStatsBar({
  totalElements,
  accounts,
}: AccountStatsBarProps) {
  const activeCount = accounts.filter((account) => account.status === 'ACTIVE').length
  const deletedCount = accounts.filter((account) => account.status === 'DELETED').length

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
      {/* Tổng tài khoản */}
      <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-[#5B4632]/70">Tổng tài khoản</span>
          <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#5B4632] border border-[#E8DBC5]">
            <Users className="w-4 h-4" />
          </div>
        </div>
        <div className="mt-3 flex items-baseline gap-2">
          <span className="text-2xl font-bold text-[#5B4632]">{totalElements}</span>
          <span className="text-xs font-semibold text-[#C8792C]">Bản ghi</span>
        </div>
      </div>

      {/* Đang hoạt động */}
      <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-[#5B4632]/70">Đang hoạt động</span>
          <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-4 h-4" />
          </div>
        </div>
        <div className="mt-3 flex items-baseline gap-2">
          <span className="text-2xl font-bold text-[#5B4632]">
            {activeCount}
          </span>
          <span className="text-xs font-medium text-emerald-700">trong trang này</span>
        </div>
      </div>

      {/* Đã xóa */}
      <div className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs">
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-[#5B4632]/70">Đã xóa mềm</span>
          <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#C8792C] border border-[#E8DBC5]">
            <Trash2 className="w-4 h-4" />
          </div>
        </div>
        <div className="mt-3 flex items-baseline gap-2">
          <span className="text-2xl font-bold text-[#5B4632]">{deletedCount}</span>
          <span className="text-xs font-semibold text-[#5B4632]/60">trong trang này</span>
        </div>
      </div>
    </div>
  )
}

