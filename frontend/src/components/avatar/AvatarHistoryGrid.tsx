import {
  CheckCircle2,
  Clock3,
  Eye,
  RotateCcw,
  Trash2,
  User,
} from "lucide-react";
import type { AvatarResponse } from "@/types/avatar";

type AvatarHistoryGridProps = {
  history: AvatarResponse[];
  images: Record<number, string>;
  description?: string;
  actionId?: number | null;
  onRestore?: (avatarId: number) => void;
  onDelete?: (avatarId: number) => void;
  deleteLabel?: string;
};

export default function AvatarHistoryGrid({
  history,
  images,
  description = "Xóa ảnh avatar",
  actionId = null,
  onRestore,
  onDelete,
  deleteLabel = "Xóa",
}: AvatarHistoryGridProps) {
  return (
    <section className="rounded-2xl bg-white border border-[#E8DBC5] p-6">
      <div className="flex items-center justify-between mb-5">
        <div>
          <h3 className="text-base font-bold text-[#5B4632]">Lịch sử avatar</h3>
          <p className="text-xs text-[#5B4632]/60 mt-1">
            {history.length} ảnh đã lưu{description ? ` · ${description}` : ""}
          </p>
        </div>
        <Clock3 className="w-5 h-5 text-[#C8792C]" />
      </div>
      {history.length === 0 ? (
        <div className="py-10 text-center text-xs text-[#5B4632]/50">
          Chưa có lịch sử avatar
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {history.map((avatar) => (
            <div
              key={avatar.id}
              className={`rounded-xl border p-3 ${avatar.current ? "border-[#C8792C] bg-[#FFF9F1]" : "border-[#E8DBC5]"}`}
            >
              <div className="aspect-square rounded-lg overflow-hidden bg-[#FAF6EE] flex items-center justify-center">
                {images[avatar.id] ? (
                  <img
                    src={images[avatar.id]}
                    alt={avatar.originalFileName}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-8 h-8 text-[#5B4632]/30" />
                )}
              </div>
              <div className="mt-3">
                <div className="flex items-center justify-between gap-2">
                  <p className="truncate text-xs font-bold text-[#5B4632]">
                    {avatar.originalFileName}
                  </p>
                  {avatar.current && (
                    <span className="text-[10px] text-emerald-700 font-semibold">
                      Hiện tại
                    </span>
                  )}
                </div>
                <p className="text-[11px] text-[#5B4632]/60 mt-1">
                  {new Date(avatar.uploadedAt).toLocaleString("vi-VN")} ·{" "}
                  {(avatar.fileSize / 1024 / 1024).toFixed(2)} MB
                </p>
                <p className="text-[11px] text-[#5B4632]/60">
                  {avatar.contentType} · {avatar.status}
                </p>
                <div className="flex flex-wrap items-center gap-x-3 gap-y-2 mt-3">
                  {images[avatar.id] && (
                    <a
                      href={images[avatar.id]}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex items-center gap-1 text-[11px] text-[#C8792C]"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      Xem
                    </a>
                  )}
                  {!avatar.current &&
                    avatar.status === "ACTIVE" &&
                    onRestore && (
                      <button
                        type="button"
                        disabled={actionId === avatar.id}
                        onClick={() => onRestore(avatar.id)}
                        className="inline-flex items-center gap-1 text-[11px] text-emerald-700 cursor-pointer disabled:opacity-50"
                      >
                        <RotateCcw className="w-3.5 h-3.5" />
                        Đặt làm avatar
                      </button>
                    )}
                  {onDelete && (
                    <button
                      type="button"
                      disabled={actionId === avatar.id}
                      onClick={() => onDelete(avatar.id)}
                      className="inline-flex items-center gap-1 text-[11px] text-rose-600 cursor-pointer disabled:opacity-50"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      {deleteLabel}
                    </button>
                  )}
                  {avatar.current && !onDelete && (
                    <span className="inline-flex items-center gap-1 text-[10px] text-emerald-700">
                      <CheckCircle2 className="w-3 h-3" />
                      Hiện tại
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
