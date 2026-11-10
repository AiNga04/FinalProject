import { Search } from "lucide-react";
import type { AccountType, Status } from "@/types/account";
import CustomSelect from '@/components/common/CustomSelect'
import CustomDatePicker from '@/components/common/CustomDatePicker'

interface AccountFilterBarProps {
  searchTerm: string;
  selectedRole: string;
  selectedStatus: string;
  totalElements: number;
  onSearchChange: (value: string) => void;
  onRoleChange: (value: string) => void;
  onStatusChange: (value: string) => void;
  minCreatedDate: string;
  maxCreatedDate: string;
  onMinCreatedDateChange: (value: string) => void;
  onMaxCreatedDateChange: (value: string) => void;
}

const ROLES: { value: AccountType | "ALL"; label: string }[] = [
  { value: "ALL", label: "Tất cả vai trò" },
  { value: "ADMIN", label: "ADMIN" },
  { value: "MANAGER", label: "MANAGER" },
  { value: "EMPLOYEE", label: "EMPLOYEE" },
  { value: "USER", label: "USER" },
];

const STATUSES: { value: Status; label: string }[] = [
  { value: "ACTIVE", label: "Đang hoạt động (ACTIVE)" },
  { value: "INACTIVE", label: "Không hoạt động (INACTIVE)" },
  { value: "DELETED", label: "Đã xóa (DELETED)" },
];

export default function AccountFilterBar({
  searchTerm,
  selectedRole,
  selectedStatus,
  totalElements,
  onSearchChange,
  onRoleChange,
  onStatusChange,
  minCreatedDate,
  maxCreatedDate,
  onMinCreatedDateChange,
  onMaxCreatedDateChange,
}: AccountFilterBarProps) {
  return (
    <div className="p-4 border-b border-[#E8DBC5] bg-[#FAF6EE]/50 flex flex-wrap items-center justify-between gap-3">
      <div className="flex flex-wrap items-center gap-3">
        <span className="text-xs font-semibold text-[#5B4632]">Lọc nhanh:</span>

        {/* Tìm kiếm */}
        <div className="relative w-60">
          <Search className="w-3.5 h-3.5 text-[#5B4632]/50 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Tìm username, email, tên..."
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full pl-8 pr-3 py-1.5 text-xs rounded-xl bg-white border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C]"
          />
        </div>

        {/* Lọc vai trò */}
        <CustomSelect value={selectedRole} onChange={onRoleChange} options={ROLES} className="w-32" />

        {/* Lọc trạng thái */}
        <CustomSelect value={selectedStatus} onChange={onStatusChange} options={STATUSES} className="w-52" />
      </div>

      <div className="flex items-center gap-2 w-full lg:w-auto">
        <CustomDatePicker value={minCreatedDate} onChange={onMinCreatedDateChange} ariaLabel="Từ ngày tạo" />
        <span className="text-xs text-[#5B4632]/60">đến</span>
        <CustomDatePicker value={maxCreatedDate} onChange={onMaxCreatedDateChange} ariaLabel="Đến ngày tạo" />
      </div>

      <span className="text-xs text-[#5B4632]/70 font-medium">
        Tổng cộng: <strong className="text-[#C8792C]">{totalElements}</strong>{" "}
        tài khoản
      </span>
    </div>
  );
}
