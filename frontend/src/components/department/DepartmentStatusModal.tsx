import { useState } from "react";
import { Loader2, RotateCcw, Trash2, X } from "lucide-react";
import type { Status } from "@/types/account";

interface Props {
  title: string;
  message: string;
  status: Status;
  onClose: () => void;
  onConfirm: () => Promise<void>;
}
export default function DepartmentStatusModal({
  title,
  message,
  status,
  onClose,
  onConfirm,
}: Props) {
  const [loading, setLoading] = useState(false);
  const confirm = async () => {
    setLoading(true);
    try {
      await onConfirm();
      onClose();
    } finally {
      setLoading(false);
    }
  };
  const danger = status === "DELETED";
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-sm w-full p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div
              className={`p-2.5 rounded-xl ${danger ? "bg-rose-50 text-rose-600" : "bg-emerald-50 text-emerald-600"}`}
            >
              {danger ? (
                <Trash2 className="w-5 h-5" />
              ) : (
                <RotateCcw className="w-5 h-5" />
              )}
            </div>
            <h3 className="font-bold text-base text-[#5B4632]">{title}</h3>
          </div>
          <button type="button" onClick={onClose} className="cursor-pointer">
            <X className="w-4 h-4" />
          </button>
        </div>
        <p className="mt-4 text-xs text-[#5B4632]/80">{message}</p>
        <div className="flex justify-end gap-2 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs border border-[#E8DBC5] rounded-xl cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={confirm}
            disabled={loading}
            className={`inline-flex items-center gap-2 px-5 py-2 text-xs font-bold text-white rounded-xl cursor-pointer disabled:opacity-60 ${danger ? "bg-rose-600" : "bg-emerald-600"}`}
          >
            {loading && <Loader2 className="w-3.5 h-3.5 animate-spin" />}Xác
            nhận
          </button>
        </div>
      </div>
    </div>
  );
}
