import { useCallback, useEffect, useState } from 'react'
import { ArrowLeft, Clock3, Eye, Loader2, Mail, RotateCcw, ShieldCheck, Trash2, User } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { accountApi } from '@/api/accountApi'
import { avatarApi } from '@/api/avatarApi'
import type { Account } from '@/types/account'
import type { AvatarResponse } from '@/types/avatar'
import ConfirmDialog from '@/components/common/ConfirmDialog'

export default function AccountDetailPage() {
  const { id } = useParams()
  const accountId = Number(id)
  const [account, setAccount] = useState<Account | null>(null)
  const [history, setHistory] = useState<AvatarResponse[]>([])
  const [images, setImages] = useState<Record<number, string>>({})
  const [loading, setLoading] = useState(true)
  const [actionId, setActionId] = useState<number | null>(null)
  const [pendingAction, setPendingAction] = useState<{ avatarId: number; action: 'delete' | 'restore' } | null>(null)

  const load = useCallback(async () => {
    const [accountResponse, historyResponse] = await Promise.all([
      accountApi.getAccountById(accountId),
      avatarApi.getHistoryByAccount(accountId),
    ])
    setAccount(accountResponse.data)
    setHistory(historyResponse.data)
    const entries = await Promise.all(historyResponse.data.map(async (avatar) => {
      try { return [avatar.id, URL.createObjectURL(await avatarApi.getFile(avatar.id))] as const } catch { return null }
    }))
    setImages(Object.fromEntries(entries.filter((entry): entry is readonly [number, string] => entry !== null)))
  }, [accountId])

  useEffect(() => { let cancelled = false; void load().finally(() => { if (!cancelled) setLoading(false) }); return () => { cancelled = true } }, [load])

  const runAction = async () => {
    if (!pendingAction) return
    const { avatarId, action } = pendingAction
    setActionId(avatarId)
    try {
      if (action === 'delete') await avatarApi.removeByAccount(accountId, avatarId)
      else await avatarApi.restore(accountId, avatarId)
      await load()
    } finally { setActionId(null) }
  }

  if (loading) return <div className="flex justify-center py-20"><Loader2 className="animate-spin text-[#C8792C]" /></div>
  if (!account) return <p className="text-sm text-rose-600">Không tìm thấy tài khoản.</p>
  const currentAvatar = history.find((avatar) => avatar.current && avatar.status === 'ACTIVE')

  return <div className="space-y-6">
    <Link to="/accounts" className="inline-flex items-center gap-2 text-xs font-semibold text-[#C8792C]"><ArrowLeft className="w-4 h-4" />Quay lại danh sách</Link>
    <div><span className="text-xs font-semibold uppercase tracking-wider text-[#C8792C]">Chi tiết tài khoản</span><h2 className="text-2xl font-bold text-[#5B4632] mt-1">{account.username}</h2></div>
    <section className="rounded-2xl bg-white border border-[#E8DBC5] p-6"><div className="flex flex-col sm:flex-row items-center sm:items-start gap-6"><div className="w-28 h-28 rounded-3xl overflow-hidden bg-[#FAF6EE] border border-[#E8DBC5] text-3xl font-bold text-[#C8792C] flex items-center justify-center">{currentAvatar && images[currentAvatar.id] ? <img src={images[currentAvatar.id]} alt="Avatar" className="w-full h-full object-cover" /> : account.username.charAt(0).toUpperCase()}</div><div className="flex-1 text-center sm:text-left"><h3 className="text-xl font-bold text-[#5B4632]">{account.lastName} {account.firstName}</h3><p className="text-sm text-[#5B4632]/60 mt-1">@{account.username}</p><span className="inline-flex items-center gap-1 mt-3 px-3 py-1 rounded-full bg-[#C8792C]/10 text-[#C8792C] text-xs font-bold"><ShieldCheck className="w-3.5 h-3.5" />{account.accountType}</span></div></div><div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-8 pt-6 border-t border-[#E8DBC5]"><div className="flex gap-3"><User className="w-4 h-4 text-[#C8792C]" /><div><p className="text-[11px] text-[#5B4632]/50">Họ và tên</p><p className="text-sm font-semibold text-[#5B4632]">{account.lastName} {account.firstName}</p></div></div><div className="flex gap-3"><Mail className="w-4 h-4 text-[#C8792C]" /><div><p className="text-[11px] text-[#5B4632]/50">Email</p><p className="text-sm font-semibold text-[#5B4632]">{account.email}</p></div></div><div><p className="text-[11px] text-[#5B4632]/50">Phòng ban</p><p className="text-sm font-semibold text-[#5B4632]">{account.departmentName ?? account.departmentId ?? 'Chưa phân phòng ban'}</p></div><div><p className="text-[11px] text-[#5B4632]/50">Trạng thái</p><p className="text-sm font-semibold text-emerald-700">{account.status}</p></div></div></section>
    <section className="rounded-2xl bg-white border border-[#E8DBC5] p-6"><div className="flex items-center justify-between mb-5"><div><h3 className="text-base font-bold text-[#5B4632]">Lịch sử avatar</h3><p className="text-xs text-[#5B4632]/60 mt-1">{history.length} ảnh đã lưu · Xóa sẽ xóa vĩnh viễn</p></div><Clock3 className="w-5 h-5 text-[#C8792C]" /></div>{history.length === 0 ? <div className="py-10 text-center text-xs text-[#5B4632]/50">Chưa có lịch sử avatar</div> : <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">{history.map((avatar) => <div key={avatar.id} className={`rounded-xl border p-3 ${avatar.current ? 'border-[#C8792C] bg-[#FFF9F1]' : 'border-[#E8DBC5]'}`}><div className="aspect-square rounded-lg overflow-hidden bg-[#FAF6EE] flex items-center justify-center">{images[avatar.id] ? <img src={images[avatar.id]} alt={avatar.originalFileName} className="w-full h-full object-cover" /> : <User className="w-8 h-8 text-[#5B4632]/30" />}</div><div className="mt-3"><div className="flex items-center justify-between gap-2"><p className="truncate text-xs font-bold text-[#5B4632]">{avatar.originalFileName}</p>{avatar.current && <span className="text-[10px] text-emerald-700 font-semibold">Hiện tại</span>}</div><p className="text-[11px] text-[#5B4632]/60 mt-1">{new Date(avatar.uploadedAt).toLocaleString('vi-VN')} · {(avatar.fileSize / 1024 / 1024).toFixed(2)} MB</p><p className="text-[11px] text-[#5B4632]/60">{avatar.contentType} · {avatar.status}</p><div className="flex items-center gap-2 mt-3">{images[avatar.id] && <a href={images[avatar.id]} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-[11px] text-[#C8792C]"><Eye className="w-3.5 h-3.5" />Xem</a>}{!avatar.current && avatar.status === 'ACTIVE' && <button type="button" disabled={actionId === avatar.id} onClick={() => setPendingAction({ avatarId: avatar.id, action: 'restore' })} className="inline-flex items-center gap-1 text-[11px] text-emerald-700 cursor-pointer"><RotateCcw className="w-3.5 h-3.5" />Đặt làm avatar</button>}<button type="button" disabled={actionId === avatar.id} onClick={() => setPendingAction({ avatarId: avatar.id, action: 'delete' })} className="inline-flex items-center gap-1 text-[11px] text-rose-600 cursor-pointer"><Trash2 className="w-3.5 h-3.5" />Xóa vĩnh viễn</button></div></div></div>)}</div>}</section>
    <ConfirmDialog open={pendingAction !== null} title={pendingAction?.action === 'delete' ? 'Xóa avatar vĩnh viễn' : 'Đặt làm avatar hiện tại'} message={pendingAction?.action === 'delete' ? 'Ảnh sẽ bị xóa khỏi database và storage, không thể khôi phục.' : 'Ảnh được chọn sẽ trở thành avatar hiện tại của tài khoản.'} confirmLabel={pendingAction?.action === 'delete' ? 'Xóa vĩnh viễn' : 'Đặt làm avatar'} tone={pendingAction?.action === 'delete' ? 'danger' : 'success'} onClose={() => setPendingAction(null)} onConfirm={runAction} />
  </div>
}
