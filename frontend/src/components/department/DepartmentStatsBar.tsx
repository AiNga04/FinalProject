import { Building2, Users, UserCheck } from "lucide-react";
import type { Department } from "@/types/department";

export default function DepartmentStatsBar({
  total,
  departments,
}: {
  total: number;
  departments: Department[];
}) {
  const active = departments.filter((item) => item.status === "ACTIVE").length;
  const members = departments.reduce(
    (sum, item) => sum + (item.totalMember || 0),
    0,
  );
  const cards = [
    ["Tổng phòng ban", total, Building2, "text-[#5B4632]"],
    ["Đang hoạt động", active, UserCheck, "text-emerald-700"],
    ["Tổng thành viên", members, Users, "text-[#C8792C]"],
  ] as const;
  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
      {cards.map(([label, value, Icon, color]) => (
        <div
          key={label}
          className="p-5 rounded-2xl bg-white border border-[#E8DBC5] shadow-xs"
        >
          <div className="flex items-center justify-between">
            <p className="text-xs text-[#5B4632]/70">{label}</p>
            <Icon className={`w-5 h-5 ${color}`} />
          </div>
          <strong className={`block mt-3 text-2xl ${color}`}>{value}</strong>
        </div>
      ))}
    </div>
  );
}
