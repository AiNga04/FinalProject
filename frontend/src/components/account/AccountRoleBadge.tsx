import { ShieldCheck } from "lucide-react";
import type { AccountType } from "@/types/account";

interface AccountRoleBadgeProps {
  role: AccountType;
}

const ROLE_COLOR: Record<AccountType, string> = {
  ADMIN: "text-rose-700 bg-rose-50 border-rose-200",
  MANAGER: "text-purple-700 bg-purple-50 border-purple-200",
  EMPLOYEE: "text-[#5B4632] bg-[#FAF6EE] border-[#E8DBC5]",
  USER: "text-sky-700 bg-sky-50 border-sky-200",
};

export default function AccountRoleBadge({ role }: AccountRoleBadgeProps) {
  const colorClass =
    ROLE_COLOR[role] ?? "text-[#5B4632] bg-[#FAF6EE] border-[#E8DBC5]";

  return (
    <span
      className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold border ${colorClass}`}
    >
      <ShieldCheck className="w-3.5 h-3.5" />
      {role}
    </span>
  );
}
