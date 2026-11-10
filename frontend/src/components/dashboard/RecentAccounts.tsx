import { ArrowUpRight } from "lucide-react";
import { Link } from "react-router-dom";
import type { Account } from "@/types/account";
export default function RecentAccounts({ accounts }: { accounts: Account[] }) {
  return (
    <div className="lg:col-span-2 p-6 rounded-2xl bg-white border border-[#E8DBC5]/60 shadow-xs">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-[#5B4632]">
            Tài khoản mới nhất
          </h3>
          <p className="text-xs text-gray-500">
            Dữ liệu lấy trực tiếp từ hệ thống
          </p>
        </div>
        <Link
          to="/accounts"
          className="text-xs font-semibold text-[#C8792C] flex items-center gap-1"
        >
          Xem tất cả <ArrowUpRight className="w-3.5 h-3.5" />
        </Link>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="border-b border-gray-100 text-gray-400">
              <th className="pb-3">Họ và tên</th>
              <th className="pb-3">Username</th>
              <th className="pb-3">Vai trò</th>
              <th className="pb-3 text-right">Trạng thái</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {accounts.length === 0 ? (
              <tr>
                <td colSpan={4} className="py-8 text-center text-gray-400">
                  Chưa có tài khoản
                </td>
              </tr>
            ) : (
              accounts.map((account) => (
                <tr key={account.id}>
                  <td className="py-3 font-semibold text-[#5B4632]">
                    {account.lastName} {account.firstName}
                  </td>
                  <td className="py-3 text-gray-500">@{account.username}</td>
                  <td className="py-3">
                    <span className="px-2 py-0.5 rounded-md text-[10px] font-bold bg-gray-100 text-gray-600">
                      {account.accountType}
                    </span>
                  </td>
                  <td className="py-3 text-right">
                    <span
                      className={`inline-block w-2 h-2 rounded-full mr-1.5 ${account.status === "ACTIVE" ? "bg-emerald-500" : "bg-gray-300"}`}
                    />
                    {account.status}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
