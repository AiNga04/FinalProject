import { Link } from "react-router-dom";
import { Home } from "lucide-react";

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#FAF6EE] text-[#3D2D20] p-4 text-center">
      <div className="w-16 h-16 rounded-2xl bg-[#C8792C]/10 text-[#C8792C] flex items-center justify-center font-black text-3xl mb-4 border border-[#C8792C]/30">
        404
      </div>
      <h1 className="text-2xl font-bold text-[#5B4632] mb-2">
        Trang Không Tồn Tại
      </h1>
      <p className="text-xs text-[#5B4632]/70 max-w-sm mb-6">
        Đường dẫn bạn yêu cầu không tồn tại hoặc đã được di chuyển sang địa chỉ
        khác trong hệ thống.
      </p>
      <Link
        to="/dashboard"
        className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#C8792C] hover:bg-[#B26822] text-white font-bold text-xs shadow-md shadow-[#C8792C]/30 transition"
      >
        <Home className="w-4 h-4" />
        <span>Về Trang Chủ</span>
      </Link>
    </div>
  );
}
