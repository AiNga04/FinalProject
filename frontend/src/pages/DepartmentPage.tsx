import { useCallback, useEffect, useState } from 'react'
import { Plus } from 'lucide-react'
import { departmentApi } from '@/api/departmentApi'
import type { Department, CreateDepartmentRequest, UpdateDepartmentRequest } from '@/types/department'
import type { Status } from '@/types/account'
import DepartmentStatsBar from '@/components/department/DepartmentStatsBar'
import DepartmentFilterBar from '@/components/department/DepartmentFilterBar'
import DepartmentTable from '@/components/department/DepartmentTable'
import DepartmentFormModal from '@/components/department/DepartmentFormModal'
import DepartmentStatusModal from '@/components/department/DepartmentStatusModal'

const PAGE_SIZE = 10

export default function DepartmentPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [search, setSearch] = useState('')
  const [type, setType] = useState('')
  const [status, setStatus] = useState<Status | 'ALL'>('ACTIVE')
  const [minCreatedDate, setMinCreatedDate] = useState('')
  const [maxCreatedDate, setMaxCreatedDate] = useState('')
  const [selected, setSelected] = useState<number[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formTarget, setFormTarget] = useState<Department | 'create' | null>(null)
  const [statusDialog, setStatusDialog] = useState<{ ids: number[]; status: Status; title: string; message: string } | null>(null)

  const fetchDepartments = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const response = await departmentApi.getDepartments({ pageNumber: page, size: PAGE_SIZE, search: search || undefined, type: type || undefined, status: status === 'ALL' ? undefined : status, minCreatedDate: minCreatedDate || undefined, maxCreatedDate: maxCreatedDate || undefined })
      setDepartments(response.data.items); setTotal(response.data.totalElements); setTotalPages(response.data.totalPages || 1)
    } catch { setError('Không thể tải danh sách phòng ban') } finally { setLoading(false) }
  }, [page, search, type, status, minCreatedDate, maxCreatedDate])
  useEffect(() => { fetchDepartments() }, [fetchDepartments])

  const toggleAll = () => { const ids = departments.map((item) => item.id); setSelected((current) => ids.every((id) => current.includes(id)) ? current.filter((id) => !ids.includes(id)) : [...new Set([...current, ...ids])]) }
  const openSingleStatus = (item: Department, nextStatus: Status) => setStatusDialog({ ids: [item.id], status: nextStatus, title: nextStatus === 'DELETED' ? 'Xóa phòng ban' : 'Khôi phục phòng ban', message: nextStatus === 'DELETED' ? `Phòng ban “${item.name}” sẽ được xóa mềm.` : `Phòng ban “${item.name}” sẽ được chuyển về ACTIVE.` })
  const confirmStatus = async () => { if (!statusDialog) return; if (statusDialog.ids.length === 1) await departmentApi.updateStatus(statusDialog.ids[0]!, statusDialog.status); else await departmentApi.updateStatusList(statusDialog.ids, statusDialog.status); setSelected([]); await fetchDepartments() }
  const submitForm = async (data: CreateDepartmentRequest | UpdateDepartmentRequest) => { if (formTarget === 'create') await departmentApi.createDepartment(data as CreateDepartmentRequest); else if (formTarget) await departmentApi.updateDepartment(formTarget.id, data as UpdateDepartmentRequest); await fetchDepartments() }

  const actionOptions: Status[] = ['ACTIVE', 'INACTIVE', 'DELETED']
  return <div className="space-y-6"><div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4"><div><span className="text-xs font-semibold uppercase tracking-wider text-[#C8792C]">TestingSystem Portal</span><h2 className="text-2xl font-bold text-[#5B4632] mt-1">Quản lý Phòng ban</h2></div><button type="button" onClick={() => setFormTarget('create')} className="inline-flex items-center justify-center gap-2 px-5 py-2.5 text-xs font-bold text-white bg-[#C8792C] rounded-xl cursor-pointer"><Plus className="w-4 h-4" />Thêm phòng ban</button></div>
    {error && <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-xs text-amber-800">{error}</div>}
    <DepartmentStatsBar total={total} departments={departments} />
    <div className="rounded-2xl bg-white border border-[#E8DBC5] overflow-visible"><DepartmentFilterBar search={search} type={type} status={status} minCreatedDate={minCreatedDate} maxCreatedDate={maxCreatedDate} onSearch={(value) => { setSearch(value); setPage(1) }} onType={(value) => { setType(value); setPage(1) }} onStatus={(value) => { setStatus(value); setPage(1) }} onMinDate={(value) => { setMinCreatedDate(value); setPage(1) }} onMaxDate={(value) => { setMaxCreatedDate(value); setPage(1) }} />
      {selected.length > 0 && <div className="flex items-center gap-3 px-4 py-2.5 bg-[#C8792C]/10 border-b border-[#C8792C]/20 text-xs"><b className="text-[#C8792C]">Đã chọn {selected.length} phòng ban</b><select defaultValue="" onChange={(event) => { const next = event.target.value as Status; if (next) setStatusDialog({ ids: selected, status: next, title: 'Cập nhật trạng thái', message: `Bạn có chắc muốn cập nhật ${selected.length} phòng ban đã chọn?` }) }} className="px-3 py-1.5 rounded-lg border border-[#E8DBC5] text-xs"><option value="">Hành động</option>{actionOptions.map((action) => <option key={action} value={action} disabled={action === status}>{action === 'ACTIVE' ? 'Kích hoạt' : action === 'INACTIVE' ? 'Vô hiệu hóa' : 'Xóa mềm'}</option>)}</select><button type="button" onClick={() => setSelected([])} className="ml-auto text-[#5B4632]/60 cursor-pointer">Bỏ chọn</button></div>}
      <DepartmentTable departments={departments} loading={loading} selected={selected} onToggle={(id) => setSelected((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id])} onToggleAll={toggleAll} onEdit={setFormTarget} onDelete={(item) => openSingleStatus(item, 'DELETED')} onRestore={(item) => openSingleStatus(item, 'ACTIVE')} />
      <div className="p-4 flex items-center justify-between text-xs text-[#5B4632]/70"><span>Trang {page} / {totalPages} · {total} phòng ban</span><div className="flex gap-2"><button disabled={page <= 1} onClick={() => setPage((value) => value - 1)} className="px-3 py-1.5 border border-[#E8DBC5] rounded-lg disabled:opacity-40 cursor-pointer">Trước</button><button disabled={page >= totalPages} onClick={() => setPage((value) => value + 1)} className="px-3 py-1.5 border border-[#E8DBC5] rounded-lg disabled:opacity-40 cursor-pointer">Sau</button></div></div>
    </div>
    {formTarget && <DepartmentFormModal department={formTarget === 'create' ? undefined : formTarget} onClose={() => setFormTarget(null)} onSubmit={submitForm} />}
    {statusDialog && <DepartmentStatusModal title={statusDialog.title} message={statusDialog.message} status={statusDialog.status} onClose={() => setStatusDialog(null)} onConfirm={confirmStatus} />}
  </div>
}
