import { useEffect, useMemo, useRef, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, X } from "lucide-react";

interface Props {
  value: string;
  onChange: (value: string) => void;
  ariaLabel: string;
}
const WEEKDAYS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
const pad = (value: number) => String(value).padStart(2, "0");
const toKey = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
const parse = (value: string) => {
  const [year, month, day] = value.split("-").map(Number);
  return year && month && day ? new Date(year, month - 1, day) : new Date();
};

export default function CustomDatePicker({
  value,
  onChange,
  ariaLabel,
}: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const initial = parse(value);
  const [open, setOpen] = useState(false);
  const [month, setMonth] = useState(
    new Date(initial.getFullYear(), initial.getMonth(), 1),
  );
  useEffect(() => {
    const close = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node))
        setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);
  const days = useMemo(() => {
    const first = new Date(month.getFullYear(), month.getMonth(), 1);
    const start = new Date(
      month.getFullYear(),
      month.getMonth(),
      1 - first.getDay(),
    );
    return Array.from(
      { length: 42 },
      (_, index) =>
        new Date(
          start.getFullYear(),
          start.getMonth(),
          start.getDate() + index,
        ),
    );
  }, [month]);
  const display = value
    ? `${value.slice(5, 7)}/${value.slice(8, 10)}/${value.slice(0, 4)}`
    : "mm/dd/yyyy";
  const select = (date: Date) => {
    onChange(toKey(date));
    setOpen(false);
  };
  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        aria-label={ariaLabel}
        onClick={() => setOpen((current) => !current)}
        className="w-[145px] flex items-center justify-between gap-2 text-xs bg-white border border-[#E8DBC5] rounded-xl px-3 py-2 text-[#3D2D20] hover:border-[#C8792C] cursor-pointer"
      >
        <span className={value ? "" : "text-[#5B4632]/60"}>{display}</span>
        <CalendarDays className="w-3.5 h-3.5" />
      </button>
      {open && (
        <div className="absolute right-0 top-full mt-2 z-50 w-64 rounded-2xl border border-[#E8DBC5] bg-white p-3 shadow-xl">
          <div className="flex items-center justify-between mb-3">
            <button
              type="button"
              onClick={() =>
                setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))
              }
              className="p-1.5 rounded-lg hover:bg-[#FAF6EE] cursor-pointer"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <strong className="text-xs text-[#3D2D20]">
              {month.toLocaleDateString("en-US", {
                month: "long",
                year: "numeric",
              })}
            </strong>
            <button
              type="button"
              onClick={() =>
                setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))
              }
              className="p-1.5 rounded-lg hover:bg-[#FAF6EE] cursor-pointer"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
          <div className="grid grid-cols-7 gap-1 text-center">
            {WEEKDAYS.map((day) => (
              <span
                key={day}
                className="py-1 text-[10px] font-semibold text-[#5B4632]/50"
              >
                {day}
              </span>
            ))}
            {days.map((day) => {
              const key = toKey(day);
              const currentMonth = day.getMonth() === month.getMonth();
              const selected = key === value;
              return (
                <button
                  type="button"
                  key={key}
                  onClick={() => select(day)}
                  className={`h-7 rounded-lg text-[11px] cursor-pointer ${currentMonth ? "text-[#3D2D20]" : "text-[#5B4632]/30"} ${selected ? "bg-[#C8792C] text-white font-bold" : "hover:bg-[#FAF6EE]"}`}
                >
                  {day.getDate()}
                </button>
              );
            })}
          </div>
          <div className="flex justify-between mt-3 pt-2 border-t border-[#E8DBC5]">
            <button
              type="button"
              onClick={() => {
                onChange("");
                setOpen(false);
              }}
              className="inline-flex items-center gap-1 text-[11px] text-[#5B4632]/70 hover:text-rose-600 cursor-pointer"
            >
              <X className="w-3 h-3" />
              Xóa
            </button>
            <button
              type="button"
              onClick={() => select(new Date())}
              className="text-[11px] text-[#C8792C] font-semibold cursor-pointer"
            >
              Hôm nay
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
