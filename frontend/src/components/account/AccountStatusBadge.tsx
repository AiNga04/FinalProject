import { CheckCircle2, XCircle, Clock, Trash2 } from 'lucide-react'
import type { Status } from '@/types/account'

interface AccountStatusBadgeProps {
  status: Status
}

const STATUS_CONFIG: Record<Status, { label: string; className: string; icon: React.ReactNode }> = {
  ACTIVE: {
    label: 'Hoạt động',
    className: 'bg-emerald-50 text-emerald-800 border-emerald-200',
    icon: <CheckCircle2 className="w-3 h-3 text-emerald-600" />,
  },
  INACTIVE: {
    label: 'Không hoạt động',
    className: 'bg-amber-50 text-amber-800 border-amber-200',
    icon: <Clock className="w-3 h-3 text-amber-600" />,
  },
  DELETED: {
    label: 'Đã xóa',
    className: 'bg-zinc-100 text-zinc-600 border-zinc-200',
    icon: <Trash2 className="w-3 h-3 text-zinc-500" />,
  },
}

export default function AccountStatusBadge({ status }: AccountStatusBadgeProps) {
  // Fallback cho các status không có trong config
  const config = STATUS_CONFIG[status] ?? {
    label: status,
    className: 'bg-zinc-100 text-zinc-600 border-zinc-200',
    icon: <XCircle className="w-3 h-3 text-zinc-500" />,
  }

  return (
    <span
      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${config.className}`}
    >
      {config.icon}
      {config.label}
    </span>
  )
}

