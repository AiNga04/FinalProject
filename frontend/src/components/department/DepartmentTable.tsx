import { Edit, Loader2, RotateCcw, Trash2 } from "lucide-react";
import type { Department } from "@/types/department";
import type { Status } from "@/types/account";

function Badge({ status }: { status: Status }) {
  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-semibold border ${status === "ACTIVE" ? "bg-emerald-50 text-emerald-700 border-emerald-200" : status === "DELETED" ? "bg-zinc-100 text-zinc-600 border-zinc-200" : "bg-amber-50 text-amber-700 border-amber-200"}`}
    >
      {status === "ACTIVE"
        ? "Hoạt động"
        : status === "DELETED"
          ? "Đã xóa"
          : "Không hoạt động"}
    </span>
  );
}

interface Props {
  departments: Department[];
  loading: boolean;
  selected: number[];
  onToggle: (id: number) => void;
  onToggleAll: () => void;
  onEdit: (item: Department) => void;
  onDelete: (item: Department) => void;
  onRestore: (item: Department) => void;
}
export default function DepartmentTable({
  departments,
  loading,
  selected,
  onToggle,
  onToggleAll,
  onEdit,
  onDelete,
  onRestore,
}: Props) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-xs">
        <thead className="bg-[#FAF6EE] border-b border-[#E8DBC5]">
          <tr>
            <th className="p-4">
              <input
                type="checkbox"
                checked={
                  departments.length > 0 &&
                  departments.every((item) => selected.includes(item.id))
                }
                onChange={onToggleAll}
                className="accent-[#C8792C]"
              />
            </th>
            {[
              "ID",
              "Tên phòng ban",
              "Loại",
              "Thành viên",
              "Trạng thái",
              "Ngày tạo",
              "Thao tác",
            ].map((header) => (
              <th key={header} className="p-4 font-bold text-[#5B4632]">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-[#E8DBC5]/60">
          {loading ? (
            <tr>
              <td colSpan={8} className="p-10 text-center">
                <Loader2 className="inline w-5 h-5 animate-spin text-[#C8792C]" />
              </td>
            </tr>
          ) : departments.length === 0 ? (
            <tr>
              <td colSpan={8} className="p-10 text-center text-[#5B4632]/50">
                Không có phòng ban
              </td>
            </tr>
          ) : (
            departments.map((item) => (
              <tr key={item.id} className="hover:bg-[#FAF6EE]/50">
                <td className="p-4">
                  <input
                    type="checkbox"
                    checked={selected.includes(item.id)}
                    onChange={() => onToggle(item.id)}
                    className="accent-[#C8792C]"
                  />
                </td>
                <td className="p-4 text-[#5B4632]/60">#{item.id}</td>
                <td className="p-4 font-bold text-[#5B4632]">{item.name}</td>
                <td className="p-4">{item.type}</td>
                <td className="p-4">{item.totalMember}</td>
                <td className="p-4">
                  <Badge status={item.status} />
                </td>
                <td className="p-4">
                  {item.createdAt
                    ? new Date(item.createdAt).toLocaleDateString("vi-VN")
                    : "—"}
                </td>
                <td className="p-4">
                  <div className="flex gap-2">
                    {item.status === "DELETED" ? (
                      <button
                        type="button"
                        onClick={() => onRestore(item)}
                        className="text-emerald-600 cursor-pointer"
                      >
                        <RotateCcw className="w-4 h-4" />
                      </button>
                    ) : (
                      <>
                        <button
                          type="button"
                          onClick={() => onEdit(item)}
                          className="text-[#5B4632]/70 cursor-pointer"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => onDelete(item)}
                          className="text-rose-600 cursor-pointer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
