import type { LucideIcon } from "lucide-react";
interface Stat {
  title: string;
  value: number;
  subtext: string;
  icon: LucideIcon;
  color: string;
  bg: string;
}
export default function DashboardStats({ stats }: { stats: Stat[] }) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((item) => (
        <div
          key={item.title}
          className="p-5 rounded-2xl bg-white border border-[#E8DBC5]/60 shadow-xs"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#5B4632]/70">
              {item.title}
            </span>
            <div className={`p-2.5 rounded-xl ${item.bg} ${item.color}`}>
              <item.icon className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-2xl font-bold text-[#5B4632]">
              {item.value}
            </span>
            <p className="mt-1 text-[11px] font-medium text-[#5B4632]/60">
              {item.subtext}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
