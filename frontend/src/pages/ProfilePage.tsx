import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, Loader2, Mail, ShieldCheck, User } from "lucide-react";
import { avatarApi } from "@/api/avatarApi";
import { authApi } from "@/api/authApi";
import type { Account } from "@/types/account";
import type { AvatarResponse } from "@/types/avatar";
import ConfirmDialog from "@/components/common/ConfirmDialog";
import AvatarHistoryGrid from "@/components/avatar/AvatarHistoryGrid";

export default function ProfilePage() {
  const [user, setUser] = useState<Account | null>(null);
  const [history, setHistory] = useState<AvatarResponse[]>([]);
  const [images, setImages] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [pendingAvatar, setPendingAvatar] = useState<number | null>(null);
  const [pendingDeleteAvatar, setPendingDeleteAvatar] = useState<number | null>(
    null,
  );
  const fileRef = useRef<HTMLInputElement>(null);

  const load = useCallback(async () => {
    const [accountResponse, historyResponse] = await Promise.all([
      authApi.getMe(),
      avatarApi.getMyHistory(),
    ]);
    setUser(accountResponse.data);
    setHistory(historyResponse.data);
    const entries = await Promise.all(
      historyResponse.data.map(async (avatar) => {
        try {
          return [
            avatar.id,
            URL.createObjectURL(await avatarApi.getMyFile(avatar.id)),
          ] as const;
        } catch {
          return null;
        }
      }),
    );
    setImages(
      Object.fromEntries(
        entries.filter(
          (entry): entry is readonly [number, string] => entry !== null,
        ),
      ),
    );
  }, []);
  useEffect(() => {
    let cancelled = false;
    void load().finally(() => {
      if (!cancelled) setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [load]);

  const upload = async (file?: File) => {
    if (!file) return;
    setUploading(true);
    try {
      await avatarApi.upload(file);
      await load();
    } finally {
      setUploading(false);
    }
  };
  const restore = async () => {
    if (pendingAvatar) {
      await avatarApi.restoreMine(pendingAvatar);
      await load();
    }
  };
  const removeHistoryAvatar = async () => {
    if (pendingDeleteAvatar === null) return;
    await avatarApi.removeMine(pendingDeleteAvatar);
    setPendingDeleteAvatar(null);
    await load();
  };
  if (loading)
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="animate-spin text-[#C8792C]" />
      </div>
    );
  if (!user)
    return (
      <p className="text-sm text-rose-600">
        Không thể tải thông tin tài khoản.
      </p>
    );
  const current = history.find(
    (avatar) => avatar.current && avatar.status === "ACTIVE",
  );

  return (
    <div className="max-w-5xl space-y-6">
      <div>
        <span className="text-xs font-semibold uppercase tracking-wider text-[#C8792C]">
          Tài khoản cá nhân
        </span>
        <h2 className="text-2xl font-bold text-[#5B4632] mt-1">
          Hồ sơ của tôi
        </h2>
      </div>
      <section className="rounded-2xl bg-white border border-[#E8DBC5] p-6">
        <div className="flex flex-col md:flex-row items-center md:items-start gap-8">
          <div className="relative">
            <div className="w-36 h-36 rounded-3xl overflow-hidden bg-[#FAF6EE] border border-[#E8DBC5] text-4xl font-bold text-[#C8792C] flex items-center justify-center">
              {current && images[current.id] ? (
                <img
                  src={images[current.id]}
                  alt="Avatar"
                  className="w-full h-full object-cover"
                />
              ) : (
                user.username.charAt(0).toUpperCase()
              )}
            </div>
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              disabled={uploading}
              className="absolute -bottom-3 -right-3 inline-flex items-center gap-1 rounded-xl bg-[#C8792C] px-3 py-2 text-[11px] font-bold text-white shadow-md cursor-pointer disabled:opacity-60"
            >
              <Camera className="w-3.5 h-3.5" />
              Đổi ảnh
            </button>
            <input
              ref={fileRef}
              type="file"
              accept="image/png,image/jpeg,image/webp"
              className="hidden"
              onChange={(event) => {
                void upload(event.target.files?.[0]);
                event.currentTarget.value = "";
              }}
            />
          </div>
          <div className="flex-1 text-center md:text-left">
            <h3 className="text-2xl font-bold text-[#5B4632]">
              {user.lastName} {user.firstName}
            </h3>
            <p className="text-sm text-[#5B4632]/60 mt-1">@{user.username}</p>
            <span className="inline-flex items-center gap-1 mt-3 px-3 py-1 rounded-full bg-[#C8792C]/10 text-[#C8792C] text-xs font-bold">
              <ShieldCheck className="w-3.5 h-3.5" />
              {user.accountType}
            </span>
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-8 pt-6 border-t border-[#E8DBC5]">
          <div className="flex gap-3">
            <User className="w-4 h-4 text-[#C8792C]" />
            <div>
              <p className="text-[11px] text-[#5B4632]/50">Họ và tên</p>
              <p className="text-sm font-semibold text-[#5B4632]">
                {user.lastName} {user.firstName}
              </p>
            </div>
          </div>
          <div className="flex gap-3">
            <Mail className="w-4 h-4 text-[#C8792C]" />
            <div>
              <p className="text-[11px] text-[#5B4632]/50">Email</p>
              <p className="text-sm font-semibold text-[#5B4632]">
                {user.email}
              </p>
            </div>
          </div>
          <div>
            <p className="text-[11px] text-[#5B4632]/50">Trạng thái</p>
            <p className="text-sm font-semibold text-emerald-700">
              {user.status}
            </p>
          </div>
          <div>
            <p className="text-[11px] text-[#5B4632]/50">Ngày tạo</p>
            <p className="text-sm font-semibold text-[#5B4632]">
              {new Date(user.createdAt).toLocaleDateString("vi-VN")}
            </p>
          </div>
        </div>
      </section>
      <AvatarHistoryGrid
        history={history}
        images={images}
        onRestore={(avatarId) => setPendingAvatar(avatarId)}
        onDelete={(avatarId) => setPendingDeleteAvatar(avatarId)}
      />
      <ConfirmDialog
        open={pendingAvatar !== null}
        title="Đặt làm avatar hiện tại"
        message="Ảnh được chọn sẽ trở thành avatar hiện tại."
        confirmLabel="Đặt làm avatar"
        tone="success"
        onClose={() => setPendingAvatar(null)}
        onConfirm={restore}
      />
      <ConfirmDialog
        open={pendingDeleteAvatar !== null}
        title="Xóa ảnh avatar"
        message="Ảnh này sẽ bị xóa khỏi lịch sử avatar và không thể khôi phục. Bạn có chắc chắn muốn tiếp tục?"
        confirmLabel="Xóa"
        tone="danger"
        onClose={() => setPendingDeleteAvatar(null)}
        onConfirm={removeHistoryAvatar}
      />
    </div>
  );
}
