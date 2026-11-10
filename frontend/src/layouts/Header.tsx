import { Search, Bell, Menu, Camera, Trash2 } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useAuthStore } from "@/stores/useAuthStore";
import { avatarApi } from "@/api/avatarApi";
import { Link } from "react-router-dom";

interface HeaderProps {
  searchTerm?: string;
  onSearchChange?: (value: string) => void;
  searchPlaceholder?: string;
  onMenuOpen?: () => void;
}

export default function Header({
  searchTerm = "",
  onSearchChange,
  searchPlaceholder = "Tìm kiếm trong hệ thống...",
  onMenuOpen,
}: HeaderProps) {
  const { user } = useAuthStore();
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null);
  const [avatarMenuOpen, setAvatarMenuOpen] = useState(false);
  const [avatarLoading, setAvatarLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    const load = async () => {
      if (!user) return;
      try {
        const current = await avatarApi.getCurrent();
        if (!current.data || cancelled) return;
        const file = await avatarApi.getCurrentFile();
        if (!cancelled) {
          objectUrl = URL.createObjectURL(file);
          setAvatarUrl(objectUrl);
        }
      } catch {
        if (!cancelled) setAvatarUrl(null);
      }
    };
    void load();
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [user?.id]);

  const uploadAvatar = async (file?: File) => {
    if (!file) return;
    setAvatarLoading(true);
    try {
      await avatarApi.upload(file);
      const blob = await avatarApi.getCurrentFile();
      if (avatarUrl) URL.revokeObjectURL(avatarUrl);
      setAvatarUrl(URL.createObjectURL(blob));
      setAvatarMenuOpen(false);
    } finally {
      setAvatarLoading(false);
    }
  };

  const removeAvatar = async () => {
    setAvatarLoading(true);
    try {
      await avatarApi.remove();
      if (avatarUrl) URL.revokeObjectURL(avatarUrl);
      setAvatarUrl(null);
      setAvatarMenuOpen(false);
    } finally {
      setAvatarLoading(false);
    }
  };

  return (
    <header className="h-20 bg-white border-b border-[#E8DBC5] px-4 sm:px-6 lg:px-8 flex items-center gap-3 justify-between sticky top-0 z-30 shadow-xs">
      <button
        type="button"
        onClick={onMenuOpen}
        aria-label="Mở menu"
        className="lg:hidden shrink-0 p-2 rounded-xl border border-[#E8DBC5] text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
      >
        <Menu className="w-5 h-5" />
      </button>
      <div className="relative flex-1 max-w-96">
        <Search className="w-4 h-4 text-[#5B4632]/50 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => onSearchChange?.(e.target.value)}
          placeholder={searchPlaceholder}
          className="w-full pl-10 pr-4 py-2 text-xs rounded-xl bg-[#FAF6EE] border border-[#E8DBC5] text-[#3D2D20] placeholder-[#5B4632]/40 outline-none focus:border-[#C8792C]"
        />
      </div>
      <div className="flex items-center gap-4">
        <button
          type="button"
          title="Thông báo"
          className="relative p-2.5 rounded-xl border border-[#E8DBC5] text-[#5B4632] hover:bg-[#FAF6EE] cursor-pointer"
        >
          <Bell className="w-4 h-4" />
          <span className="w-2 h-2 rounded-full bg-[#C8792C] absolute top-2 right-2" />
        </button>
        {user && (
          <div className="relative flex items-center gap-2 pl-2 border-l border-[#E8DBC5]">
            <button
              type="button"
              onClick={() => setAvatarMenuOpen((open) => !open)}
              title="Quản lý avatar"
              className="w-9 h-9 rounded-xl overflow-hidden border border-[#E8DBC5] bg-[#FAF6EE] text-[#5B4632] font-bold text-xs flex items-center justify-center cursor-pointer"
            >
              {avatarUrl ? (
                <img
                  src={avatarUrl}
                  alt="Avatar"
                  className="w-full h-full object-cover"
                />
              ) : (
                user.username.charAt(0).toUpperCase()
              )}
            </button>
            <span className="hidden sm:inline text-xs font-bold text-[#5B4632]">
              {user.firstName
                ? `${user.lastName} ${user.firstName}`
                : user.username}
            </span>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#C8792C]/10 text-[#C8792C] font-mono font-bold border border-[#C8792C]/30">
              {user.accountType}
            </span>
            {avatarMenuOpen && (
              <div className="absolute right-0 top-full mt-2 z-50 w-48 rounded-xl border border-[#E8DBC5] bg-white p-2 shadow-xl">
                <Link
                  to="/profile"
                  onClick={() => setAvatarMenuOpen(false)}
                  className="block rounded-lg px-3 py-2 text-xs text-[#3D2D20] hover:bg-[#FAF6EE]"
                >
                  Xem hồ sơ
                </Link>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/png,image/jpeg,image/webp"
                  className="hidden"
                  onChange={(event) => {
                    void uploadAvatar(event.target.files?.[0]);
                    event.currentTarget.value = "";
                  }}
                />
                <button
                  type="button"
                  disabled={avatarLoading}
                  onClick={() => fileInputRef.current?.click()}
                  className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-xs text-[#3D2D20] hover:bg-[#FAF6EE] cursor-pointer disabled:opacity-50"
                >
                  <Camera className="w-4 h-4 text-[#C8792C]" />
                  Đổi avatar
                </button>
                {avatarUrl && (
                  <button
                    type="button"
                    disabled={avatarLoading}
                    onClick={() => void removeAvatar()}
                    className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-xs text-rose-600 hover:bg-rose-50 cursor-pointer disabled:opacity-50"
                  >
                    <Trash2 className="w-4 h-4" />
                    Xóa avatar
                  </button>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
