import { Building, UserPlus } from "lucide-react";
import { Link } from "react-router-dom";

export default function DashboardWelcome({
  displayName,
}: {
  displayName: string;
}) {
  return (
    <div className="p-8 rounded-3xl bg-gradient-to-r from-[#423223] to-[#5B4632] text-white shadow-lg">
      <h2 className="text-2xl font-bold tracking-tight mb-2">
        Xin chào, {displayName}! 👋
      </h2>
      <p className="text-xs text-[#E8DBC5]/80 leading-relaxed mb-6">
        Đây là tổng quan dữ liệu tài khoản và phòng ban hiện tại trong
        TestingSystem.
      </p>
      <div className="flex flex-wrap items-center gap-3">
        <Link
          to="/accounts"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#C8792C] text-white font-bold text-xs"
        >
          <UserPlus className="w-4 h-4" />
          Quản lý Tài Khoản
        </Link>
        <Link
          to="/departments"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/10 text-white font-semibold text-xs border border-white/20"
        >
          <Building className="w-4 h-4" />
          Quản lý Phòng Ban
        </Link>
      </div>
    </div>
  );
}
