import { useEffect, useMemo, useState } from 'react'
import { Building2, Loader2, ShieldCheck, UserCheck, Users } from 'lucide-react'
import { accountApi } from '@/api/accountApi'
import { departmentApi } from '@/api/departmentApi'
import type { Account } from '@/types/account'
import type { Department } from '@/types/department'
import { useAuthStore } from '@/stores/useAuthStore'
import DashboardWelcome from '@/components/dashboard/DashboardWelcome'
import DashboardStats from '@/components/dashboard/DashboardStats'
import RecentAccounts from '@/components/dashboard/RecentAccounts'
import DepartmentOverview from '@/components/dashboard/DepartmentOverview'

export default function DashboardPage() {
  const { user } = useAuthStore()
  const [accounts, setAccounts] = useState<Account[]>([])
  const [departments, setDepartments] = useState<Department[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const displayName = user ? user.firstName && user.lastName ? `${user.lastName} ${user.firstName}` : user.username : 'Admin'
  useEffect(() => { let cancelled = false; Promise.all([accountApi.getAccounts({ pageNumber: 1, size: 100 }), departmentApi.getDepartments({ pageNumber: 1, size: 100, status: 'ACTIVE' })]).then(([accountsResponse, departmentsResponse]) => { if (!cancelled) { setAccounts(accountsResponse.data.items); setDepartments(departmentsResponse.data.items) } }).catch(() => { if (!cancelled) setError('Không thể tải dữ liệu tổng quan. Vui lòng thử lại sau.') }).finally(() => { if (!cancelled) setLoading(false) }); return () => { cancelled = true } }, [])
  const activeAccounts = accounts.filter((account) => account.status === 'ACTIVE')
  const adminCount = accounts.filter((account) => account.accountType === 'ADMIN' && account.status !== 'DELETED').length
  const recentAccounts = useMemo(() => [...accounts].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt)).slice(0, 5), [accounts])
  const totalMembers = departments.reduce((sum, department) => sum + (department.totalMember || 0), 0)
  const stats = [
    { title: 'Tổng tài khoản', value: accounts.length, subtext: `${accounts.filter((account) => account.status === 'DELETED').length} tài khoản đã xóa`, icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
    { title: 'Phòng ban hoạt động', value: departments.length, subtext: `${totalMembers} thành viên đang được phân bổ`, icon: Building2, color: 'text-amber-600', bg: 'bg-amber-50' },
    { title: 'Tài khoản hoạt động', value: activeAccounts.length, subtext: accounts.length ? `${Math.round(activeAccounts.length / accounts.length * 100)}% tổng tài khoản` : 'Chưa có dữ liệu', icon: UserCheck, color: 'text-emerald-600', bg: 'bg-emerald-50' },
    { title: 'Quản trị viên', value: adminCount, subtext: 'Tài khoản có quyền ADMIN', icon: ShieldCheck, color: 'text-purple-600', bg: 'bg-purple-50' },
  ]
  return <div className="space-y-6"><DashboardWelcome displayName={displayName} />{error && <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-xs text-amber-800">{error}</div>}{loading ? <div className="flex items-center justify-center py-16 text-xs text-[#5B4632]/70"><Loader2 className="w-5 h-5 animate-spin mr-2 text-[#C8792C]" />Đang tải dữ liệu tổng quan...</div> : <><DashboardStats stats={stats} /><div className="grid grid-cols-1 lg:grid-cols-3 gap-6"><RecentAccounts accounts={recentAccounts} /><DepartmentOverview departments={departments} /></div></>}</div>
}
