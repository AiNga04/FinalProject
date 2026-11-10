import { Link } from "react-router-dom";
import { ShieldAlert, Home } from "lucide-react";

export default function Forbiddent() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#FAF6EE] text-[#3D2D20] p-4 text-center">
      <div className="w-16 h-16 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mb-4 border border-rose-200 shadow-xs">
        <ShieldAlert className="w-8 h-8" />
      </div>
      <h1 className="text-2xl font-bold text-[#5B4632] mb-2">
        Truy Cập Bị Từ Chối (403)
      </h1>
      <p className="text-xs text-[#5B4632]/70 max-w-sm mb-6">
        Tài khoản của bạn không có đủ thẩm quyền (Role) để truy cập chức năng
        này trong hệ thống.
      </p>
      <Link
        to="/dashboard"
        className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#5B4632] hover:bg-[#423223] text-white font-bold text-xs shadow-md transition"
      >
        <Home className="w-4 h-4" />
        <span>Về Trang Chủ</span>
      </Link>
    </div>
  );
}
