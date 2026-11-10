import { Search } from "lucide-react";
import type { Status } from "@/types/account";
import CustomSelect from "@/components/common/CustomSelect";
import CustomDatePicker from "@/components/common/CustomDatePicker";

interface Props {
  search: string;
  type: string;
  status: Status | "ALL";
  minCreatedDate: string;
  maxCreatedDate: string;
  onSearch: (value: string) => void;
  onType: (value: string) => void;
  onStatus: (value: Status | "ALL") => void;
  onMinDate: (value: string) => void;
  onMaxDate: (value: string) => void;
}

export default function DepartmentFilterBar({
  search,
  type,
  status,
  minCreatedDate,
  maxCreatedDate,
  onSearch,
  onType,
  onStatus,
  onMinDate,
  onMaxDate,
}: Props) {
  return (
    <div className="p-4 border-b border-[#E8DBC5] bg-[#FAF6EE]/50 flex flex-wrap items-center justify-between gap-3">
      <div className="flex flex-wrap items-center gap-3">
        <span className="text-xs font-semibold text-[#5B4632]">Lọc nhanh:</span>
        <div className="relative w-60">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#5B4632]/50" />
          <input
            value={search}
            onChange={(e) => onSearch(e.target.value)}
            placeholder="Tìm tên phòng ban..."
            className="w-full pl-8 pr-3 py-2 text-xs rounded-xl bg-white border border-[#E8DBC5] outline-none focus:border-[#C8792C]"
          />
        </div>
        <input
          value={type}
          onChange={(e) => onType(e.target.value)}
          placeholder="Loại phòng ban"
          className="w-44 px-3 py-2 text-xs rounded-xl bg-white border border-[#E8DBC5] outline-none focus:border-[#C8792C]"
        />
        <CustomSelect
          value={status}
          onChange={(value) => onStatus(value as Status | "ALL")}
          options={[
            { value: "ACTIVE", label: "Đang hoạt động (ACTIVE)" },
            { value: "INACTIVE", label: "Không hoạt động (INACTIVE)" },
            { value: "DELETED", label: "Đã xóa (DELETED)" },
          ]}
          className="w-52"
        />
      </div>
      <div className="flex items-center gap-2">
        <CustomDatePicker
          value={minCreatedDate}
          onChange={onMinDate}
          ariaLabel="Từ ngày tạo"
        />
        <span className="text-xs text-[#5B4632]/60">đến</span>
        <CustomDatePicker
          value={maxCreatedDate}
          onChange={onMaxDate}
          ariaLabel="Đến ngày tạo"
        />
      </div>
    </div>
  );
}
