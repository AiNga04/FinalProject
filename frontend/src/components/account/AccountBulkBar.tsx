import { useState, useRef, useEffect, type ReactNode } from "react";
import { ChevronDown, Trash2, CheckCircle2, XCircle, X } from "lucide-react";
import type { Status } from "@/types/account";

interface BulkAction {
  status: Status;
  label: string;
  icon: ReactNode;
  colorClass: string;
}

const BULK_ACTIONS: BulkAction[] = [
  {
    status: "ACTIVE",
    label: "Kích hoạt",
    icon: <CheckCircle2 className="w-4 h-4" />,
    colorClass: "text-emerald-700 hover:bg-emerald-50",
  },
  {
    status: "INACTIVE",
    label: "Vô hiệu hóa",
    icon: <XCircle className="w-4 h-4" />,
    colorClass: "text-amber-700 hover:bg-amber-50",
  },
  {
    status: "DELETED",
    label: "Xóa",
    icon: <Trash2 className="w-4 h-4" />,
    colorClass: "text-rose-700 hover:bg-rose-50",
  },
];

interface AccountBulkBarProps {
  selectedCount: number;
  currentStatus: string;
  onAction: (status: Status) => void;
  onClearSelection: () => void;
}

export default function AccountBulkBar({
  selectedCount,
  currentStatus,
  onAction,
  onClearSelection,
}: AccountBulkBarProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  // Đóng dropdown khi click ra ngoài
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  if (selectedCount === 0) return null;

  const availableActions = BULK_ACTIONS.filter(
    (action) => currentStatus === "ALL" || action.status !== currentStatus,
  );

  return (
    <div className="flex items-center gap-3 px-4 py-2.5 bg-[#C8792C]/10 border-b border-[#C8792C]/20 animate-in slide-in-from-top-2 duration-200">
      {/* Đã chọn */}
      <span className="text-xs font-semibold text-[#C8792C]">
        Đã chọn {selectedCount} tài khoản
      </span>

      <div className="h-3.5 w-px bg-[#C8792C]/30" />

      {/* Dropdown Hành động */}
      <div className="relative" ref={ref}>
        <button
          type="button"
          onClick={() => setOpen((o) => !o)}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-[#C8792C] hover:bg-[#B26822] rounded-lg shadow-sm cursor-pointer transition"
        >
          <span>Hành động</span>
          <ChevronDown
            className={`w-3.5 h-3.5 transition-transform ${open ? "rotate-180" : ""}`}
          />
        </button>

        {open && (
          <div className="absolute left-0 top-full mt-1.5 z-30 w-44 bg-white border border-[#E8DBC5] rounded-xl shadow-lg overflow-hidden animate-in fade-in zoom-in-95 duration-100">
            {availableActions.map((action) => (
              <button
                key={action.status}
                type="button"
                onClick={() => {
                  setOpen(false);
                  onAction(action.status);
                }}
                className={`flex items-center gap-2.5 w-full px-4 py-2.5 text-xs font-medium text-left transition cursor-pointer ${action.colorClass}`}
              >
                {action.icon}
                {action.label}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Bỏ chọn */}
      <button
        type="button"
        onClick={onClearSelection}
        title="Bỏ chọn tất cả"
        className="ml-auto p-1 rounded-lg text-[#5B4632]/50 hover:text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer transition"
      >
        <X className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
