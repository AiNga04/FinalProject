import { AlertTriangle, CheckCircle2, Trash2, X, Loader2 } from "lucide-react";
import { useState, type ReactNode } from "react";
import type { Status } from "@/types/account";

interface AccountBulkStatusModalProps {
  count: number;
  status: Status;
  onClose: () => void;
  onConfirm: () => Promise<void>;
}

const CONTENT: Record<
  Status,
  {
    title: string;
    message: string;
    button: string;
    className: string;
    icon: ReactNode;
  }
> = {
  ACTIVE: {
    title: "Kích hoạt tài khoản",
    message: "Các tài khoản đã chọn sẽ được chuyển sang trạng thái ACTIVE.",
    button: "Kích hoạt",
    className: "bg-emerald-600 hover:bg-emerald-700",
    icon: <CheckCircle2 className="w-5 h-5" />,
  },
  INACTIVE: {
    title: "Vô hiệu hóa tài khoản",
    message: "Các tài khoản đã chọn sẽ được chuyển sang trạng thái INACTIVE.",
    button: "Vô hiệu hóa",
    className: "bg-amber-600 hover:bg-amber-700",
    icon: <AlertTriangle className="w-5 h-5" />,
  },
  DELETED: {
    title: "Xóa tài khoản đã chọn",
    message: "Các tài khoản sẽ được xóa và có thể khôi phục sau.",
    button: "Xóa tài khoản",
    className: "bg-rose-600 hover:bg-rose-700",
    icon: <Trash2 className="w-5 h-5" />,
  },
};

export default function AccountBulkStatusModal({
  count,
  status,
  onClose,
  onConfirm,
}: AccountBulkStatusModalProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const content = CONTENT[status];

  const handleConfirm = async () => {
    setIsSubmitting(true);
    try {
      await onConfirm();
      onClose();
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl border border-[#E8DBC5] shadow-2xl max-w-sm w-full p-6">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-[#FAF6EE] text-[#C8792C]">
              {content.icon}
            </div>
            <div>
              <h3 className="font-bold text-base text-[#5B4632]">
                {content.title}
              </h3>
              <p className="text-[11px] text-[#5B4632]/60 mt-0.5">
                Đã chọn {count} tài khoản
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
        <p className="mt-4 text-xs text-[#5B4632]/80 leading-relaxed">
          {content.message}
        </p>
        <div className="flex items-center justify-end gap-2.5 mt-6">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-[#5B4632] border border-[#E8DBC5] rounded-xl hover:bg-[#FAF6EE] cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={isSubmitting}
            className={`inline-flex items-center gap-2 px-5 py-2 text-xs font-bold text-white rounded-xl shadow-md cursor-pointer disabled:opacity-60 ${content.className}`}
          >
            {isSubmitting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
            {content.button}
          </button>
        </div>
      </div>
    </div>
  );
}
