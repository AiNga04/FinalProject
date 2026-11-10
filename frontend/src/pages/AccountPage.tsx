import { useState, useEffect, useCallback } from 'react'
import { Plus, Download, RefreshCw } from 'lucide-react'
import { accountApi } from '@/api/accountApi'
import type { Account, CreateAccountRequest, Status, UpdateAccountRequest } from '@/types/account'
import AccountStatsBar from '@/components/account/AccountStatsBar'
import AccountFilterBar from '@/components/account/AccountFilterBar'
import AccountTable from '@/components/account/AccountTable'
import AccountPagination from '@/components/account/AccountPagination'
import AccountCreateModal from '@/components/account/AccountCreateModal'
import AccountEditModal from '@/components/account/AccountEditModal'
import AccountDeleteModal from '@/components/account/AccountDeleteModal'
import AccountRestoreModal from '@/components/account/AccountRestoreModal'
import AccountBulkBar from '@/components/account/AccountBulkBar'
import AccountBulkStatusModal from '@/components/account/AccountBulkStatusModal'

export default function AccountPage() {
  // ─── State: data ─────────────────────────────────────────────────────────────
  const [accounts, setAccounts] = useState<Account[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [page, setPage] = useState(1)
  const PAGE_SIZE = 10

  // ─── State: UI ───────────────────────────────────────────────────────────────
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // ─── State: filters ──────────────────────────────────────────────────────────
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedStatus, setSelectedStatus] = useState('ACTIVE')
  const [selectedRole, setSelectedRole] = useState('ALL')
  const [minCreatedDate, setMinCreatedDate] = useState('')
  const [maxCreatedDate, setMaxCreatedDate] = useState('')

  // ─── State: modals ───────────────────────────────────────────────────────────
  const [showCreate, setShowCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<Account | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Account | null>(null)
  const [restoreTarget, setRestoreTarget] = useState<Account | null>(null)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [bulkStatus, setBulkStatus] = useState<Status | null>(null)

  // ─── Fetch ───────────────────────────────────────────────────────────────────
  const fetchAccounts = useCallback(async () => {
    setIsLoading(true)
    setErrorMessage(null)
    try {
      const res = await accountApi.getAccounts({
        pageNumber: page,
        size: PAGE_SIZE,
        search: searchTerm || undefined,
        status: selectedStatus !== 'ALL' ? (selectedStatus as Status) : undefined,
        accountType: selectedRole !== 'ALL' ? (selectedRole as Account['accountType']) : undefined,
        minCreatedDate: minCreatedDate || undefined,
        maxCreatedDate: maxCreatedDate || undefined,
      })
      const d = res.data
      setAccounts(d.items)
      setTotalElements(d.totalElements)
      setTotalPages(d.totalPages || 1)
    } catch {
      setErrorMessage('Không thể tải danh sách tài khoản. Hãy chắc chắn Backend đang chạy (:8080)!')
    } finally {
      setIsLoading(false)
    }
  }, [page, searchTerm, selectedStatus, selectedRole, minCreatedDate, maxCreatedDate])

  useEffect(() => {
    fetchAccounts()
  }, [fetchAccounts])

  // ─── Handlers ────────────────────────────────────────────────────────────────
  const handleCreate = async (data: CreateAccountRequest) => {
    await accountApi.createAccount(data)
    fetchAccounts()
  }

  const handleEdit = async (id: number, data: UpdateAccountRequest) => {
    await accountApi.updateAccount(id, data)
    fetchAccounts()
  }

  const handleDelete = async (id: number) => {
    await accountApi.updateStatus(id, 'DELETED')
    fetchAccounts()
  }

  const handleRestore = async (id: number) => {
    await accountApi.updateStatus(id, 'ACTIVE')
    fetchAccounts()
  }

  const toggleSelect = (id: number) => {
    setSelectedIds((current) => current.includes(id) ? current.filter((item) => item !== id) : [...current, id])
  }

  const toggleSelectAll = () => {
    const pageIds = accounts.map((account) => account.id)
    setSelectedIds((current) => pageIds.every((id) => current.includes(id))
      ? current.filter((id) => !pageIds.includes(id))
      : [...new Set([...current, ...pageIds])])
  }

  const handleBulkStatus = async () => {
    if (!bulkStatus || selectedIds.length === 0) return
    await accountApi.updateStatusList(selectedIds, bulkStatus)
    setSelectedIds([])
    await fetchAccounts()
  }

  const handleSearchChange = (value: string) => {
    setSearchTerm(value)
    setPage(1)
  }

  const handleRoleChange = (value: string) => {
    setSelectedRole(value)
    setPage(1)
  }

  const handleStatusChange = (value: string) => {
    setSelectedStatus(value)
    setPage(1)
  }

  // ─── Render ──────────────────────────────────────────────────────────────────
  return (
    <div className="space-y-6">
      {/* Page header */}
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
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={fetchAccounts}
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
            onClick={() => setShowCreate(true)}
            className="flex items-center gap-2 px-5 py-2.5 text-xs font-bold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-xl shadow-md shadow-[#C8792C]/30 transition hover:scale-[1.02] active:scale-95 cursor-pointer"
          >
            <Plus className="w-4 h-4 stroke-[3]" />
            <span>Thêm Tài Khoản</span>
          </button>
        </div>
      </div>

      {/* Error banner */}
      {errorMessage && (
        <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-800 text-xs flex items-center justify-between">
          <span>{errorMessage}</span>
          <button
            type="button"
            onClick={fetchAccounts}
            className="font-bold underline text-[#C8792C] ml-3 cursor-pointer"
          >
            Thử lại
          </button>
        </div>
      )}

      {/* Stats */}
      <AccountStatsBar
        totalElements={totalElements}
        accounts={accounts}
      />

      {/* Table card */}
      <div className="rounded-2xl bg-white border border-[#E8DBC5] shadow-xs overflow-hidden">
        <AccountFilterBar
          searchTerm={searchTerm}
          selectedRole={selectedRole}
          selectedStatus={selectedStatus}
          totalElements={totalElements}
          onSearchChange={handleSearchChange}
          onRoleChange={handleRoleChange}
          onStatusChange={handleStatusChange}
          minCreatedDate={minCreatedDate}
          maxCreatedDate={maxCreatedDate}
          onMinCreatedDateChange={(value) => { setMinCreatedDate(value); setPage(1) }}
          onMaxCreatedDateChange={(value) => { setMaxCreatedDate(value); setPage(1) }}
        />

        <AccountBulkBar
          selectedCount={selectedIds.length}
          currentStatus={selectedStatus}
          onAction={setBulkStatus}
          onClearSelection={() => setSelectedIds([])}
        />

        <AccountTable
          accounts={accounts}
          isLoading={isLoading}
          selectedIds={selectedIds}
          onToggleSelect={toggleSelect}
          onToggleSelectAll={toggleSelectAll}
          onEdit={setEditTarget}
          onDelete={setDeleteTarget}
          onRestore={setRestoreTarget}
        />

        <AccountPagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          onPrev={() => setPage((p) => Math.max(p - 1, 1))}
          onNext={() => setPage((p) => Math.min(p + 1, totalPages))}
        />
      </div>

      {/* Modals */}
      {showCreate && (
        <AccountCreateModal
          onClose={() => setShowCreate(false)}
          onSubmit={handleCreate}
        />
      )}

      {editTarget && (
        <AccountEditModal
          account={editTarget}
          onClose={() => setEditTarget(null)}
          onSubmit={handleEdit}
        />
      )}

      {deleteTarget && (
        <AccountDeleteModal
          account={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onConfirm={handleDelete}
        />
      )}

      {restoreTarget && (
        <AccountRestoreModal
          account={restoreTarget}
          onClose={() => setRestoreTarget(null)}
          onConfirm={handleRestore}
        />
      )}

      {bulkStatus && (
        <AccountBulkStatusModal
          count={selectedIds.length}
          status={bulkStatus}
          onClose={() => setBulkStatus(null)}
          onConfirm={handleBulkStatus}
        />
      )}
    </div>
  )
}
