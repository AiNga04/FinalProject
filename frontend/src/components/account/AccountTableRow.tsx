import { Edit, Eye, Trash2, RotateCcw } from 'lucide-react'
import type { Account } from '@/types/account'
import AccountStatusBadge from './AccountStatusBadge'
import AccountRoleBadge from './AccountRoleBadge'
import { Link } from 'react-router-dom'

interface AccountTableRowProps {
  account: Account
  selected: boolean
  onToggleSelect: () => void
  onEdit: (account: Account) => void
  onDelete: (account: Account) => void
  onRestore: (account: Account) => void
}

export default function AccountTableRow({
  account,
  selected,
  onToggleSelect,
  onEdit,
  onDelete,
  onRestore,
}: AccountTableRowProps) {
  const isDeleted = account.status === 'DELETED'

  return (
    <tr className="hover:bg-[#FAF6EE]/60 transition">
      <td className="py-4 px-4 w-12">
        <input
          type="checkbox"
          aria-label={`Chọn tài khoản ${account.username}`}
          checked={selected}
          onChange={onToggleSelect}
          className="h-4 w-4 accent-[#C8792C] cursor-pointer"
        />
      </td>
      {/* ID */}
      <td className="py-4 px-6 font-mono text-[#5B4632]/60">#{account.id}</td>

      {/* Avatar + username + fullname */}
      <td className="py-4 px-6">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-[#FAF6EE] border border-[#E8DBC5] text-[#5B4632] font-bold flex items-center justify-center text-xs flex-shrink-0">
            {account.username.charAt(0).toUpperCase()}
          </div>
          <div>
            <Link to={`/accounts/${account.id}`} className="font-bold text-[#5B4632] hover:text-[#C8792C]">{account.username}</Link>
            <p className="text-[11px] text-[#5B4632]/70">
              {account.lastName} {account.firstName}
            </p>
          </div>
        </div>
      </td>

      {/* Email */}
      <td className="py-4 px-6 font-mono text-[#5B4632]/80 text-xs">{account.email}</td>

      {/* Vai trò */}
      <td className="py-4 px-6">
        <AccountRoleBadge role={account.accountType} />
      </td>

      {/* Trạng thái */}
      <td className="py-4 px-6">
        <AccountStatusBadge status={account.status} />
      </td>

      {/* Ngày tạo */}
      <td className="py-4 px-6 font-mono text-[11px] text-[#5B4632]/70">
        {account.createdAt ? new Date(account.createdAt).toLocaleDateString('vi-VN') : '—'}
      </td>

      {/* Thao tác */}
      <td className="py-4 px-6 text-right">
        <div className="inline-flex items-center gap-1">
          {isDeleted ? (
            /* Nếu đã xóa thì chỉ cho phép khôi phục */
            <button
              type="button"
              onClick={() => onRestore(account)}
              title="Khôi phục tài khoản"
              className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-emerald-600 hover:bg-emerald-50 transition cursor-pointer"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          ) : (
            <>
              <Link
                to={`/accounts/${account.id}`}
                title="Xem chi tiết"
                className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-[#C8792C] hover:bg-[#FAF6EE] transition cursor-pointer"
              >
                <Eye className="w-4 h-4" />
              </Link>
              <button
                type="button"
                onClick={() => onEdit(account)}
                title="Chỉnh sửa"
                className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-[#C8792C] hover:bg-[#FAF6EE] transition cursor-pointer"
              >
                <Edit className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => onDelete(account)}
                title="Xóa tài khoản"
                className="p-1.5 rounded-lg text-[#5B4632]/70 hover:text-rose-600 hover:bg-rose-50 transition cursor-pointer"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </>
          )}
        </div>
      </td>
    </tr>
  )
}

