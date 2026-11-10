import { Loader2 } from 'lucide-react'
import type { Account } from '@/types/account'
import AccountTableRow from './AccountTableRow'

interface AccountTableProps {
  accounts: Account[]
  isLoading: boolean
  selectedIds: number[]
  onToggleSelect: (id: number) => void
  onToggleSelectAll: () => void
  onEdit: (account: Account) => void
  onDelete: (account: Account) => void
  onRestore: (account: Account) => void
}

const TABLE_HEADERS = ['ID', 'Tài Khoản & Tên', 'Email', 'Vai Trò', 'Trạng Thái', 'Ngày Tạo', 'Thao Tác']

export default function AccountTable({
  accounts,
  isLoading,
  selectedIds,
  onToggleSelect,
  onToggleSelectAll,
  onEdit,
  onDelete,
  onRestore,
}: AccountTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-xs">
        <thead className="bg-[#FAF6EE] text-[#5B4632] uppercase tracking-wider border-b border-[#E8DBC5] font-bold">
          <tr>
            <th className="py-3.5 px-4 w-12">
              <input
                type="checkbox"
                aria-label="Chọn tất cả tài khoản trong trang"
                checked={accounts.length > 0 && accounts.every((account) => selectedIds.includes(account.id))}
                onChange={onToggleSelectAll}
                className="h-4 w-4 accent-[#C8792C] cursor-pointer"
              />
            </th>
            {TABLE_HEADERS.map((h) => (
              <th
                key={h}
                className={`py-3.5 px-6${h === 'Thao Tác' ? ' text-right' : ''}`}
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>

        <tbody className="divide-y divide-[#E8DBC5]/50">
          {isLoading ? (
            <tr>
              <td colSpan={8} className="text-center py-12 text-[#5B4632]">
                <div className="flex items-center justify-center gap-2">
                  <Loader2 className="w-5 h-5 animate-spin text-[#C8792C]" />
                  <span className="text-xs font-semibold">Đang tải dữ liệu...</span>
                </div>
              </td>
            </tr>
          ) : accounts.length === 0 ? (
            <tr>
              <td colSpan={8} className="text-center py-12 text-[#5B4632]/50">
                Chưa có tài khoản nào hoặc không tìm thấy kết quả phù hợp.
              </td>
            </tr>
          ) : (
            accounts.map((acc) => (
              <AccountTableRow
                key={acc.id}
                account={acc}
                selected={selectedIds.includes(acc.id)}
                onToggleSelect={() => onToggleSelect(acc.id)}
                onEdit={onEdit}
                onDelete={onDelete}
                onRestore={onRestore}
              />
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}

