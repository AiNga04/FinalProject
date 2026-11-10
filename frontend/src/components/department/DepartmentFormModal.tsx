import { useState } from "react";
import { Building2, Loader2, X } from "lucide-react";
import type {
  Department,
  CreateDepartmentRequest,
  UpdateDepartmentRequest,
} from "@/types/department";

interface Props {
  department?: Department | undefined;
  onClose: () => void;
  onSubmit: (
    data: CreateDepartmentRequest | UpdateDepartmentRequest,
  ) => Promise<void>;
}
export default function DepartmentFormModal({
  department,
  onClose,
  onSubmit,
}: Props) {
  const [name, setName] = useState(department?.name ?? "");
  const [type, setType] = useState(department?.type ?? "");
  const [accountIds, setAccountIds] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const inputClass =
    "w-full text-xs px-3.5 py-2.5 rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] outline-none focus:border-[#C8792C]";
  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await onSubmit({
        name: name.trim(),
        type: type.trim(),
        ...(!department && {
          accountIds: accountIds
            .split(",")
            .map((value) => Number(value.trim()))
            .filter((value) => Number.isInteger(value) && value > 0),
        }),
      });
      onClose();
    } catch (reason) {
      setError(
        reason instanceof Error ? reason.message : "Không thể lưu phòng ban",
      );
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-md w-full p-6">
        <div className="flex items-center justify-between pb-4 border-b border-[#E8DBC5]">
          <div className="flex items-center gap-2">
            <Building2 className="w-5 h-5 text-[#C8792C]" />
            <h3 className="font-bold text-base text-[#5B4632]">
              {department ? "Chỉnh sửa phòng ban" : "Tạo phòng ban mới"}
            </h3>
          </div>
          <button type="button" onClick={onClose} className="cursor-pointer">
            <X className="w-5 h-5" />
          </button>
        </div>
        {error && (
          <p className="mt-3 text-xs text-rose-600 bg-rose-50 rounded-xl px-3 py-2">
            {error}
          </p>
        )}
        <form onSubmit={submit} autoComplete="off" className="mt-5 space-y-4">
          <input
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Ví dụ: Phòng Kỹ thuật"
            className={inputClass}
          />
          <input
            required
            value={type}
            onChange={(e) => setType(e.target.value)}
            placeholder="Ví dụ: TECHNICAL"
            className={inputClass}
          />
          {!department && (
            <div>
              <input
                value={accountIds}
                onChange={(e) => setAccountIds(e.target.value)}
                placeholder="Account IDs, ví dụ: 1, 2, 3"
                className={inputClass}
              />
              <p className="mt-1 text-[11px] text-[#5B4632]/60">
                Có thể để trống nếu chưa gán tài khoản.
              </p>
            </div>
          )}
          <div className="flex justify-end gap-2 pt-4 border-t border-[#E8DBC5]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs border border-[#E8DBC5] rounded-xl cursor-pointer"
            >
              Hủy
            </button>
            <button
              disabled={loading}
              className="inline-flex items-center gap-2 px-5 py-2 text-xs font-bold text-white bg-[#C8792C] rounded-xl cursor-pointer disabled:opacity-60"
            >
              {loading && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              {department ? "Lưu thay đổi" : "Tạo phòng ban"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
