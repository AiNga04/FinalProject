import { Link } from "react-router-dom";
import type { Department } from "@/types/department";
export default function DepartmentOverview({
  departments,
}: {
  departments: Department[];
}) {
  const max = Math.max(...departments.map((item) => item.totalMember || 0), 1);
  return (
    <div className="p-6 rounded-2xl bg-white border border-[#E8DBC5]/60 shadow-xs">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-[#5B4632]">
            Phân bổ thành viên
          </h3>
          <p className="text-xs text-gray-500">Theo phòng ban đang hoạt động</p>
        </div>
        <Link
          to="/departments"
          className="text-xs font-semibold text-[#C8792C]"
        >
          Chi tiết
        </Link>
      </div>
      <div className="space-y-4">
        {departments.length === 0 ? (
          <p className="text-xs text-gray-400">Chưa có phòng ban hoạt động</p>
        ) : (
          departments.slice(0, 6).map((department) => (
            <div key={department.id} className="space-y-1.5">
              <div className="flex justify-between text-xs font-medium">
                <span className="text-[#5B4632] truncate mr-2">
                  {department.name}
                </span>
                <span className="text-gray-500 shrink-0">
                  {department.totalMember} người
                </span>
              </div>
              <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-[#C8792C] rounded-full"
                  style={{
                    width: `${Math.round(((department.totalMember || 0) / max) * 100)}%`,
                  }}
                />
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
