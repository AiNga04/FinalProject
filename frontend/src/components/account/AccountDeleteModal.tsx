import { AlertTriangle, X, Loader2 } from "lucide-react";
import { useState } from "react";
import type { Account } from "@/types/account";

interface AccountDeleteModalProps {
  account: Account;
  onClose: () => void;
  onConfirm: (id: number) => Promise<void>;
}

export default function AccountDeleteModal({
  account,
  onClose,
  onConfirm,
}: AccountDeleteModalProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleConfirm = async () => {
    setIsSubmitting(true);
    try {
      await onConfirm(account.id);
      onClose();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-sm w-full p-6 animate-in fade-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-rose-50 text-rose-600">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-base text-[#5B4632]">
                Xác Nhận Xóa
              </h3>
              <p className="text-[11px] text-[#5B4632]/60 mt-0.5">
                Thao tác này có thể khôi phục
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg text-[#5B4632]/60 hover:bg-[#FAF6EE] cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <p className="mt-4 text-xs text-[#5B4632]/80 leading-relaxed">
          Bạn có chắc muốn xóa tài khoản{" "}
          <span className="font-bold text-[#5B4632]">@{account.username}</span>?
          <br />
          Tài khoản sẽ được chuyển sang trạng thái <strong>DELETED</strong> và
          có thể khôi phục sau.
        </p>

        {/* Footer */}
        <div className="flex items-center justify-end gap-2.5 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-[#5B4632] bg-white border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={isSubmitting}
            className="inline-flex items-center gap-2 px-5 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-xl shadow-md shadow-rose-600/30 cursor-pointer disabled:opacity-60"
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            Xóa Tài Khoản
          </button>
        </div>
      </div>
    </div>
  );
}
