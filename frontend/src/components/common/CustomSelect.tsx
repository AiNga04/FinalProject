import { useEffect, useRef, useState } from "react";
import { ChevronDown } from "lucide-react";

interface Option {
  value: string;
  label: string;
}
interface Props {
  value: string;
  options: Option[];
  onChange: (value: string) => void;
  className?: string;
}

export default function CustomSelect({
  value,
  options,
  onChange,
  className = "",
}: Props) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const selected = options.find((option) => option.value === value);
  useEffect(() => {
    const close = (event: MouseEvent) => {
      if (ref.current && !ref.current.contains(event.target as Node))
        setOpen(false);
    };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);
  return (
    <div ref={ref} className={`relative ${className}`}>
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        className="w-full flex items-center justify-between gap-3 text-xs bg-white border border-[#E8DBC5] rounded-xl px-3 py-2 text-[#3D2D20] font-medium outline-none hover:border-[#C8792C] focus:border-[#C8792C] cursor-pointer"
      >
        <span>{selected?.label}</span>
        <ChevronDown
          className={`w-3.5 h-3.5 text-[#5B4632]/60 transition-transform ${open ? "rotate-180" : ""}`}
        />
      </button>
      {open && (
        <div className="absolute left-0 top-full mt-1 z-40 w-full min-w-max overflow-hidden rounded-xl border border-[#E8DBC5] bg-white shadow-lg p-1">
          {options.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => {
                onChange(option.value);
                setOpen(false);
              }}
              className={`block w-full text-left px-3 py-2 text-xs rounded-lg cursor-pointer ${option.value === value ? "bg-[#C8792C]/10 text-[#C8792C] font-semibold" : "text-[#3D2D20] hover:bg-[#FAF6EE]"}`}
            >
              {option.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
